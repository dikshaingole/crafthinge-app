package com.crafthinge.common.events;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * LOCAL mode receiver. /internal/** is never routed by the gateway/Ingress, and
 * in AKS a NetworkPolicy allows only order-service to reach it.
 */
@RestController
@ConditionalOnProperty(name = "crafthinge.events.mode", havingValue = "http", matchIfMissing = true)
public class InternalEventController {

    private final ObjectProvider<OrderEventHandler> handlerProvider;

    public InternalEventController(ObjectProvider<OrderEventHandler> handlerProvider) {
        this.handlerProvider = handlerProvider;
    }

    @PostMapping("/internal/events")
    public ResponseEntity<Void> receive(@RequestBody OrderEvent event) {
        OrderEventHandler handler = handlerProvider.getIfAvailable();
        if (handler == null) {
            return ResponseEntity.notFound().build();
        }
        handler.handle(event);
        return ResponseEntity.accepted().build();
    }
}
