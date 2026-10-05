package com.crafthinge.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Published by order-service, consumed by delivery-service and notification-service.
 * eventId lets consumers ignore duplicates (Service Bus delivers at-least-once).
 */
public record OrderEvent(
    String type,              // ORDER_PLACED | ORDER_STATUS_CHANGED
    UUID eventId,
    Instant occurredAt,
    Long orderId,
    UUID customerId,
    Long restaurantId,
    String status,
    BigDecimal total,
    String deliveryAddress) {

    public static final String ORDER_PLACED = "ORDER_PLACED";
    public static final String ORDER_STATUS_CHANGED = "ORDER_STATUS_CHANGED";

    public static OrderEvent of(String type, Long orderId, UUID customerId, Long restaurantId,
                                String status, BigDecimal total, String deliveryAddress) {
        return new OrderEvent(type, UUID.randomUUID(), Instant.now(), orderId, customerId,
            restaurantId, status, total, deliveryAddress);
    }
}
