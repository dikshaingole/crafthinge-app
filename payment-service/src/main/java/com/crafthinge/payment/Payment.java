package com.crafthinge.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    private UUID id;
    @Column(name = "order_id")
    private Long orderId;
    @Column(name = "customer_id")
    private UUID customerId;
    private BigDecimal amount;
    private String currency = "USD";
    private String status;
    private String provider;
    @Column(name = "provider_ref")
    private String providerRef;
    @Column(name = "card_last4")
    private String cardLast4;
    @Column(name = "failure_reason")
    private String failureReason;
    @Column(name = "created_at")
    private Instant createdAt;

    protected Payment() { }

    public Payment(Long orderId, UUID customerId, BigDecimal amount, String provider,
                   PaymentProvider.Result result, String cardLast4) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.provider = provider;
        this.status = result.succeeded() ? "SUCCEEDED" : "FAILED";
        this.providerRef = result.reference();
        this.failureReason = result.failureReason();
        this.cardLast4 = cardLast4;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Long getOrderId() { return orderId; }
    public UUID getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getStatus() { return status; }
    public String getProviderRef() { return providerRef; }
    public String getCardLast4() { return cardLast4; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
}
