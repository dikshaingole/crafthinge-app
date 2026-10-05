package com.crafthinge.order;

import static com.crafthinge.order.OrderStatus.Actor.CUSTOMER;
import static com.crafthinge.order.OrderStatus.Actor.DELIVERY;
import static com.crafthinge.order.OrderStatus.Actor.RESTAURANT;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OrderStatusTest {

    @Test
    void happyPathIsAllowedForTheRightActors() {
        assertThat(OrderStatus.PLACED.canMoveTo(OrderStatus.ACCEPTED, RESTAURANT)).isTrue();
        assertThat(OrderStatus.ACCEPTED.canMoveTo(OrderStatus.PREPARING, RESTAURANT)).isTrue();
        assertThat(OrderStatus.PREPARING.canMoveTo(OrderStatus.READY_FOR_PICKUP, RESTAURANT)).isTrue();
        assertThat(OrderStatus.READY_FOR_PICKUP.canMoveTo(OrderStatus.OUT_FOR_DELIVERY, DELIVERY)).isTrue();
        assertThat(OrderStatus.OUT_FOR_DELIVERY.canMoveTo(OrderStatus.DELIVERED, DELIVERY)).isTrue();
    }

    @Test
    void customerCanOnlyCancelBeforeRestaurantAccepts() {
        assertThat(OrderStatus.PLACED.canMoveTo(OrderStatus.CANCELLED, CUSTOMER)).isTrue();
        assertThat(OrderStatus.ACCEPTED.canMoveTo(OrderStatus.CANCELLED, CUSTOMER)).isFalse();
    }

    @Test
    void wrongActorsAndSkippedStepsAreRejected() {
        assertThat(OrderStatus.PLACED.canMoveTo(OrderStatus.DELIVERED, DELIVERY)).isFalse();
        assertThat(OrderStatus.PREPARING.canMoveTo(OrderStatus.READY_FOR_PICKUP, CUSTOMER)).isFalse();
        assertThat(OrderStatus.DELIVERED.canMoveTo(OrderStatus.CANCELLED, RESTAURANT)).isFalse();
    }
}
