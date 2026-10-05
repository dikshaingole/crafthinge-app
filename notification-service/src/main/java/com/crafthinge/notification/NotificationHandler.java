package com.crafthinge.notification;

import com.crafthinge.common.events.OrderEvent;
import com.crafthinge.common.events.OrderEventHandler;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Turns order events into customer messages. Here they are logged and kept
 * in memory (last 500) so the UI can show them. A real system would send
 * email/SMS via Azure Communication Services or SendGrid and store them in a DB.
 */
@Component
public class NotificationHandler implements OrderEventHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationHandler.class);
    private static final int MAX = 500;

    public record Notification(UUID id, UUID customerId, Long orderId, String message, Instant sentAt) { }

    private final Deque<Notification> recent = new ConcurrentLinkedDeque<>();
    private final MeterRegistry metrics;

    public NotificationHandler(MeterRegistry metrics) {
        this.metrics = metrics;
    }

    @Override
    public void handle(OrderEvent e) {
        String message = messageFor(e);
        if (message == null) {
            return;
        }
        recent.addFirst(new Notification(e.eventId(), e.customerId(), e.orderId(), message, Instant.now()));
        while (recent.size() > MAX) {
            recent.pollLast();
        }
        metrics.counter("crafthinge.notifications.sent", "status", e.status()).increment();
        log.info("Notify customer {} about order {}: {}", e.customerId(), e.orderId(), message);
    }

    static String messageFor(OrderEvent e) {
        return switch (e.status()) {
            case "PLACED" -> "Order #" + e.orderId() + " is confirmed. We've sent it to the restaurant.";
            case "ACCEPTED" -> "The restaurant accepted order #" + e.orderId() + ".";
            case "PREPARING" -> "Your food for order #" + e.orderId() + " is being prepared.";
            case "READY_FOR_PICKUP" -> "Order #" + e.orderId() + " is ready. Finding a driver now.";
            case "OUT_FOR_DELIVERY" -> "Your driver picked up order #" + e.orderId() + " and is on the way.";
            case "DELIVERED" -> "Order #" + e.orderId() + " was delivered. Enjoy your meal!";
            case "CANCELLED" -> "Order #" + e.orderId() + " was cancelled.";
            default -> null;
        };
    }

    public List<Notification> forCustomer(UUID customerId) {
        return recent.stream().filter(n -> customerId.equals(n.customerId())).limit(20).toList();
    }
}
