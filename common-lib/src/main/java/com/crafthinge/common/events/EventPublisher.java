package com.crafthinge.common.events;

/** Port: how a service announces that something happened. Two adapters below. */
public interface EventPublisher {
    void publish(OrderEvent event);
}
