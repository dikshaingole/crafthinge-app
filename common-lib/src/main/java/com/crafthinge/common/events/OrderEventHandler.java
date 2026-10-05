package com.crafthinge.common.events;

/** Implemented by services that react to order events (delivery, notification). */
public interface OrderEventHandler {
    void handle(OrderEvent event);
}
