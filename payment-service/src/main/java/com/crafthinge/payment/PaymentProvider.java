package com.crafthinge.payment;

import java.math.BigDecimal;

/** Port for the card processor. Swap MockPaymentProvider for a Stripe adapter without touching anything else. */
public interface PaymentProvider {

    String name();

    Result charge(BigDecimal amount, String currency, String cardNumber);

    record Result(boolean succeeded, String reference, String failureReason) { }
}
