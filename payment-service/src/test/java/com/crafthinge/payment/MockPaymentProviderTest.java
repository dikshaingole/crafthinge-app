package com.crafthinge.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MockPaymentProviderTest {

    private final MockPaymentProvider provider = new MockPaymentProvider();

    @Test
    void testCardSucceeds() {
        var r = provider.charge(BigDecimal.TEN, "USD", "4242 4242 4242 4242");
        assertThat(r.succeeded()).isTrue();
        assertThat(r.reference()).startsWith("mock_ch_");
    }

    @Test
    void declineCardFails() {
        var r = provider.charge(BigDecimal.TEN, "USD", "4000000000000002");
        assertThat(r.succeeded()).isFalse();
        assertThat(r.failureReason()).contains("declined");
    }

    @Test
    void invalidNumberFailsLuhn() {
        assertThat(MockPaymentProvider.luhnValid("4242424242424241")).isFalse();
        assertThat(provider.charge(BigDecimal.TEN, "USD", "1234").succeeded()).isFalse();
    }
}
