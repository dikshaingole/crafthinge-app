package com.crafthinge.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class PaymentDtos {
    private PaymentDtos() { }

    public record ChargeRequest(@NotNull Long orderId, @NotNull @DecimalMin("0.50") BigDecimal amount, @NotBlank String cardNumber) { }

    public record PaymentView(UUID id, Long orderId, BigDecimal amount, String currency, String status,
                              String cardLast4, String failureReason, Instant createdAt) {
        static PaymentView of(Payment p) {
            return new PaymentView(p.getId(), p.getOrderId(), p.getAmount(), p.getCurrency(), p.getStatus(),
                p.getCardLast4(), p.getFailureReason(), p.getCreatedAt());
        }
    }
}
