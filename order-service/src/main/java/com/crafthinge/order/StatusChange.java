package com.crafthinge.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Powers the order-tracking timeline. */
@Entity
@Table(name = "order_status_history")
public class StatusChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @Column(name = "changed_by")
    private UUID changedBy;
    @Column(name = "changed_at")
    private Instant changedAt = Instant.now();

    protected StatusChange() { }

    StatusChange(OrderStatus status, UUID changedBy) {
        this.status = status;
        this.changedBy = changedBy;
    }

    public OrderStatus getStatus() { return status; }
    public Instant getChangedAt() { return changedAt; }
}
