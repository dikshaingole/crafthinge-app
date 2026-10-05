package com.crafthinge.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "customer_id")
    private UUID customerId;
    @Column(name = "restaurant_id")
    private Long restaurantId;
    @Column(name = "restaurant_name")
    private String restaurantName;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private BigDecimal subtotal;
    @Column(name = "delivery_fee")
    private BigDecimal deliveryFee;
    private BigDecimal total;
    @Column(name = "delivery_address")
    private String deliveryAddress;
    @Column(name = "payment_id")
    private UUID paymentId;
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();
    @Version
    private long version;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false)
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false)
    @OrderBy("changedAt ASC")
    private List<StatusChange> history = new ArrayList<>();

    protected Order() { }

    public Order(UUID customerId, Long restaurantId, String restaurantName, BigDecimal subtotal,
                 BigDecimal deliveryFee, String deliveryAddress, List<OrderItem> items) {
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.subtotal = subtotal;
        this.deliveryFee = deliveryFee;
        this.total = subtotal.add(deliveryFee);
        this.deliveryAddress = deliveryAddress;
        this.items.addAll(items);
        moveTo(OrderStatus.PENDING_PAYMENT, customerId);
    }

    public void moveTo(OrderStatus next, UUID by) {
        this.status = next;
        this.updatedAt = Instant.now();
        this.history.add(new StatusChange(next, by));
    }

    public void paid(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public Long getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public Long getRestaurantId() { return restaurantId; }
    public String getRestaurantName() { return restaurantName; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public BigDecimal getTotal() { return total; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public UUID getPaymentId() { return paymentId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<OrderItem> getItems() { return items; }
    public List<StatusChange> getHistory() { return history; }
}
