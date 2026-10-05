package com.crafthinge.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class OrderDtos {
    private OrderDtos() { }

    public record PlaceOrderRequest(@NotBlank @Size(max = 255) String deliveryAddress, @NotBlank String cardNumber) { }

    public record UpdateStatusRequest(@NotNull OrderStatus status) { }

    public record ItemView(Long menuItemId, String name, String emoji, BigDecimal unitPrice, int quantity) { }

    public record HistoryView(OrderStatus status, Instant at) { }

    public record OrderView(Long id, UUID customerId, Long restaurantId, String restaurantName, OrderStatus status,
                            BigDecimal subtotal, BigDecimal deliveryFee, BigDecimal total, String deliveryAddress,
                            String paymentMessage, Instant createdAt, List<ItemView> items, List<HistoryView> history) {
        static OrderView of(Order o) {
            return of(o, null);
        }

        static OrderView of(Order o, String paymentMessage) {
            return new OrderView(o.getId(), o.getCustomerId(), o.getRestaurantId(), o.getRestaurantName(), o.getStatus(),
                o.getSubtotal(), o.getDeliveryFee(), o.getTotal(), o.getDeliveryAddress(), paymentMessage, o.getCreatedAt(),
                o.getItems().stream().map(i -> new ItemView(i.getMenuItemId(), i.getName(), i.getEmoji(), i.getUnitPrice(), i.getQuantity())).toList(),
                o.getHistory().stream().map(h -> new HistoryView(h.getStatus(), h.getChangedAt())).toList());
        }
    }

    // ---- shapes returned by other services ----
    public record CartLine(Long menuItemId, String name, String emoji, BigDecimal unitPrice, int quantity) { }
    public record Cart(Long restaurantId, List<CartLine> items, BigDecimal subtotal) { }
    public record Restaurant(Long id, UUID ownerId, String name, BigDecimal deliveryFee, boolean open) { }
    public record Payment(UUID id, String status, String failureReason) { }
}
