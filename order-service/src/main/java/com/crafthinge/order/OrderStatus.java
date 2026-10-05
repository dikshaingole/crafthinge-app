package com.crafthinge.order;

import java.util.Map;
import java.util.Set;

/**
 * Order lifecycle. Each status lists who may move it where; anything else is rejected.
 *
 * PENDING_PAYMENT -> PLACED -> ACCEPTED -> PREPARING -> READY_FOR_PICKUP -> OUT_FOR_DELIVERY -> DELIVERED
 *        \-> PAYMENT_FAILED      \-> CANCELLED (customer, before the restaurant accepts; or restaurant)
 */
public enum OrderStatus {
    PENDING_PAYMENT, PAYMENT_FAILED, PLACED, ACCEPTED, PREPARING, READY_FOR_PICKUP,
    OUT_FOR_DELIVERY, DELIVERED, CANCELLED;

    public enum Actor { CUSTOMER, RESTAURANT, DELIVERY }

    private static final Map<OrderStatus, Map<Actor, Set<OrderStatus>>> ALLOWED = Map.of(
        PLACED, Map.of(
            Actor.CUSTOMER, Set.of(CANCELLED),
            Actor.RESTAURANT, Set.of(ACCEPTED, CANCELLED)),
        ACCEPTED, Map.of(Actor.RESTAURANT, Set.of(PREPARING, CANCELLED)),
        PREPARING, Map.of(Actor.RESTAURANT, Set.of(READY_FOR_PICKUP)),
        READY_FOR_PICKUP, Map.of(Actor.DELIVERY, Set.of(OUT_FOR_DELIVERY)),
        OUT_FOR_DELIVERY, Map.of(Actor.DELIVERY, Set.of(DELIVERED)));

    public boolean canMoveTo(OrderStatus next, Actor actor) {
        return ALLOWED.getOrDefault(this, Map.of()).getOrDefault(actor, Set.of()).contains(next);
    }
}
