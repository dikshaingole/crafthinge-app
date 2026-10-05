package com.crafthinge.delivery;

import com.crafthinge.common.events.OrderEvent;
import com.crafthinge.common.events.OrderEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * When the restaurant marks food READY_FOR_PICKUP, a delivery job appears for drivers.
 * Idempotent: the same event delivered twice (normal with Service Bus) creates one job.
 */
@Component
public class DeliveryEventHandler implements OrderEventHandler {

    private static final Logger log = LoggerFactory.getLogger(DeliveryEventHandler.class);

    private final DeliveryRepository repo;

    public DeliveryEventHandler(DeliveryRepository repo) {
        this.repo = repo;
    }

    @Override
    public void handle(OrderEvent event) {
        if (!OrderEvent.ORDER_STATUS_CHANGED.equals(event.type()) || !"READY_FOR_PICKUP".equals(event.status())) {
            return;
        }
        if (repo.existsByOrderId(event.orderId())) {
            log.info("Delivery for order {} already exists, ignoring duplicate event", event.orderId());
            return;
        }
        try {
            repo.save(new Delivery(event.orderId(), event.restaurantId(), event.customerId(),
                event.deliveryAddress(), event.total()));
            log.info("Delivery job created for order {}", event.orderId());
        } catch (DataIntegrityViolationException race) {
            log.info("Delivery for order {} created concurrently, ignoring", event.orderId());
        }
    }
}
