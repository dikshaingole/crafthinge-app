package com.crafthinge.common.events;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * LOCAL mode (crafthinge.events.mode=http): POSTs the event to each subscriber's
 * /internal/events endpoint. Best effort only: if a subscriber is down the
 * event is lost. That is exactly why AKS uses Azure Service Bus instead.
 */
@Component
@ConditionalOnProperty(name = "crafthinge.events.mode", havingValue = "http", matchIfMissing = true)
public class HttpEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(HttpEventPublisher.class);

    private final RestClient client;
    private final List<String> subscribers;

    public HttpEventPublisher(RestClient.Builder builder,
                              @Value("${crafthinge.events.http.subscribers:}") List<String> subscribers) {
        this.client = builder.build();
        this.subscribers = subscribers.stream().filter(s -> !s.isBlank()).toList();
    }

    @Override
    public void publish(OrderEvent event) {
        for (String base : subscribers) {
            try {
                client.post().uri(base + "/internal/events").body(event).retrieve().toBodilessEntity();
            } catch (Exception e) {
                log.warn("Could not deliver {} for order {} to {}: {}", event.type(), event.orderId(), base, e.getMessage());
            }
        }
    }
}
