package com.crafthinge.delivery;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.crafthinge.common.events.OrderEvent;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DeliveryEventHandlerTest {

    private final DeliveryRepository repo = mock(DeliveryRepository.class);
    private final DeliveryEventHandler handler = new DeliveryEventHandler(repo);

    private OrderEvent event(String status) {
        return OrderEvent.of(OrderEvent.ORDER_STATUS_CHANGED, 7L, UUID.randomUUID(), 1L, status, BigDecimal.TEN, "1 Main St");
    }

    @Test
    void readyForPickupCreatesDeliveryJob() {
        when(repo.existsByOrderId(7L)).thenReturn(false);
        handler.handle(event("READY_FOR_PICKUP"));
        verify(repo, times(1)).save(any(Delivery.class));
    }

    @Test
    void duplicateEventIsIgnored() {
        when(repo.existsByOrderId(7L)).thenReturn(true);
        handler.handle(event("READY_FOR_PICKUP"));
        verify(repo, never()).save(any(Delivery.class));
    }

    @Test
    void otherStatusesAreIgnored() {
        handler.handle(event("PREPARING"));
        verify(repo, never()).save(any(Delivery.class));
    }
}
