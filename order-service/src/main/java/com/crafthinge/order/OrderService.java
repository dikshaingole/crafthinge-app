package com.crafthinge.order;

import com.crafthinge.common.events.EventPublisher;
import com.crafthinge.common.events.OrderEvent;
import com.crafthinge.common.security.AuthUser;
import com.crafthinge.common.web.ApiException;
import com.crafthinge.order.OrderDtos.OrderView;
import com.crafthinge.order.OrderDtos.PlaceOrderRequest;
import com.crafthinge.order.OrderStatus.Actor;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Checkout orchestration. Network calls are deliberately OUTSIDE database
 * transactions (TransactionTemplate per step) so a slow payment call never
 * holds a DB connection/lock.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orders;
    private final Clients clients;
    private final EventPublisher events;
    private final TransactionTemplate tx;
    private final MeterRegistry metrics;

    public OrderService(OrderRepository orders, Clients clients, EventPublisher events,
                        TransactionTemplate tx, MeterRegistry metrics) {
        this.orders = orders;
        this.clients = clients;
        this.events = events;
        this.tx = tx;
        this.metrics = metrics;
    }

    public OrderView place(AuthUser user, PlaceOrderRequest req) {
        var cart = clients.getCart();
        if (cart == null || cart.items() == null || cart.items().isEmpty()) {
            throw ApiException.badRequest("Your cart is empty");
        }
        var restaurant = clients.getRestaurant(cart.restaurantId());
        if (!restaurant.open()) {
            throw ApiException.badRequest(restaurant.name() + " is closed right now");
        }
        var items = cart.items().stream()
            .map(l -> new OrderItem(l.menuItemId(), l.name(), l.emoji(), l.unitPrice(), l.quantity())).toList();

        Order order = tx.execute(s -> orders.save(new Order(user.id(), restaurant.id(), restaurant.name(),
            cart.subtotal(), restaurant.deliveryFee(), req.deliveryAddress(), items)));

        var payment = clients.charge(order.getId(), order.getTotal(), req.cardNumber());
        boolean paid = payment != null && "SUCCEEDED".equals(payment.status());

        Order saved = tx.execute(s -> {
            Order o = orders.findById(order.getId()).orElseThrow();
            if (paid) {
                o.paid(payment.id());
                o.moveTo(OrderStatus.PLACED, user.id());
            } else {
                o.moveTo(OrderStatus.PAYMENT_FAILED, user.id());
            }
            o.getItems().size(); // initialise lazy collections before the transaction ends
            o.getHistory().size();
            return o;
        });
        metrics.counter("crafthinge.orders.placed", "result", paid ? "paid" : "payment_failed").increment();

        if (paid) {
            clients.clearCart();
            publish(OrderEvent.ORDER_PLACED, saved);
            log.info("Order {} placed, total {}", saved.getId(), saved.getTotal());
            return OrderView.of(saved);
        }
        String reason = payment == null ? "Payment could not be processed" : payment.failureReason();
        return OrderView.of(saved, reason);
    }

    public OrderView updateStatus(AuthUser user, Long orderId, OrderStatus next) {
        Order order = orders.findById(orderId).orElseThrow(() -> ApiException.notFound("Order " + orderId));
        Actor actor = actorFor(user, order);
        if (!order.getStatus().canMoveTo(next, actor)) {
            throw ApiException.conflict("Cannot change order from " + order.getStatus() + " to " + next);
        }
        Order saved = tx.execute(s -> {
            Order o = orders.findById(orderId).orElseThrow(); // @Version guards concurrent updates
            o.moveTo(next, user.id());
            o.getItems().size();
            o.getHistory().size();
            return o;
        });
        publish(OrderEvent.ORDER_STATUS_CHANGED, saved);
        return OrderView.of(saved);
    }

    /** Who is acting on this order, and are they allowed to see it at all? */
    Actor actorFor(AuthUser user, Order order) {
        if (user.hasRole(AuthUser.RESTAURANT_OWNER)) {
            var restaurant = clients.getRestaurant(order.getRestaurantId());
            if (!user.id().equals(restaurant.ownerId())) {
                throw ApiException.forbidden("This order belongs to another restaurant");
            }
            return Actor.RESTAURANT;
        }
        if (user.hasRole(AuthUser.DELIVERY_PARTNER)) {
            return Actor.DELIVERY;
        }
        if (!order.getCustomerId().equals(user.id())) {
            throw ApiException.notFound("Order " + order.getId());
        }
        return Actor.CUSTOMER;
    }

    public OrderView get(AuthUser user, Long orderId) {
        Order order = tx.execute(s -> {
            Order o = orders.findById(orderId).orElseThrow(() -> ApiException.notFound("Order " + orderId));
            o.getItems().size();
            o.getHistory().size();
            return o;
        });
        actorFor(user, order); // access check
        return OrderView.of(order);
    }

    public List<OrderView> mine(AuthUser user) {
        return tx.execute(s -> orders.findTop50ByCustomerIdOrderByCreatedAtDesc(user.id()).stream().map(OrderView::of).toList());
    }

    public List<OrderView> forRestaurant(AuthUser user, Long restaurantId) {
        var restaurant = clients.getRestaurant(restaurantId);
        if (!user.id().equals(restaurant.ownerId())) {
            throw ApiException.forbidden("You do not own restaurant " + restaurantId);
        }
        return tx.execute(s -> orders.findTop100ByRestaurantIdAndStatusNotInOrderByCreatedAtDesc(
                restaurantId, List.of(OrderStatus.PENDING_PAYMENT, OrderStatus.PAYMENT_FAILED))
            .stream().map(OrderView::of).toList());
    }

    private void publish(String type, Order o) {
        try {
            events.publish(OrderEvent.of(type, o.getId(), o.getCustomerId(), o.getRestaurantId(),
                o.getStatus().name(), o.getTotal(), o.getDeliveryAddress()));
        } catch (Exception e) {
            // The order is saved either way. A production system would use the
            // transactional outbox pattern to guarantee the event is sent.
            log.error("Failed to publish {} for order {}", type, o.getId(), e);
        }
    }
}
