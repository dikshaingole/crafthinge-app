package com.crafthinge.delivery;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "deliveries")
public class Delivery {

    public enum Status { AWAITING_PARTNER, ASSIGNED, PICKED_UP, DELIVERED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_id")
    private Long orderId;
    @Column(name = "restaurant_id")
    private Long restaurantId;
    @Column(name = "customer_id")
    private UUID customerId;
    @Column(name = "delivery_address")
    private String deliveryAddress;
    @Column(name = "order_total")
    private BigDecimal orderTotal;
    @Column(name = "partner_id")
    private UUID partnerId;
    @Enumerated(EnumType.STRING)
    private Status status = Status.AWAITING_PARTNER;
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    protected Delivery() { }

    public Delivery(Long orderId, Long restaurantId, UUID customerId, String deliveryAddress, BigDecimal orderTotal) {
        this.orderId = orderId;
        this.restaurantId = restaurantId;
        this.customerId = customerId;
        this.deliveryAddress = deliveryAddress;
        this.orderTotal = orderTotal;
    }

    public void move(Status next) {
        this.status = next;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public Long getRestaurantId() { return restaurantId; }
    public UUID getCustomerId() { return customerId; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public BigDecimal getOrderTotal() { return orderTotal; }
    public UUID getPartnerId() { return partnerId; }
    public Status getStatus() { return status; }
    public Instant getUpdatedAt() { return updatedAt; }
}
