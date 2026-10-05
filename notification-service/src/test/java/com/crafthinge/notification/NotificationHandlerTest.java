package com.crafthinge.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.crafthinge.common.events.OrderEvent;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificationHandlerTest {

    @Test
    void storesMessageForTheRightCustomerOnly() {
        var handler = new NotificationHandler(new SimpleMeterRegistry());
        UUID alice = UUID.randomUUID();
        handler.handle(OrderEvent.of(OrderEvent.ORDER_PLACED, 5L, alice, 1L, "PLACED", BigDecimal.TEN, "x"));
        assertThat(handler.forCustomer(alice)).singleElement()
            .satisfies(n -> assertThat(n.message()).contains("#5").contains("confirmed"));
        assertThat(handler.forCustomer(UUID.randomUUID())).isEmpty();
    }
}
