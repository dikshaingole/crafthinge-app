package com.crafthinge.restaurant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;
    private String name;
    private String cuisine;
    private String description;
    private String address;
    private String city;
    private String emoji;
    private BigDecimal rating = new BigDecimal("4.5");
    @Column(name = "delivery_time_minutes")
    private int deliveryTimeMinutes = 30;
    @Column(name = "delivery_fee")
    private BigDecimal deliveryFee = new BigDecimal("2.99");
    @Column(name = "is_open")
    private boolean open = true;
    private boolean featured;
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    protected Restaurant() { }

    public Restaurant(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public void apply(RestaurantDtos.RestaurantRequest r) {
        this.name = r.name();
        this.cuisine = r.cuisine();
        this.description = r.description();
        this.address = r.address();
        this.city = r.city();
        this.emoji = r.emoji();
        this.deliveryTimeMinutes = r.deliveryTimeMinutes();
        this.deliveryFee = r.deliveryFee();
        this.open = r.open();
    }

    public Long getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getCuisine() { return cuisine; }
    public String getDescription() { return description; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getEmoji() { return emoji; }
    public BigDecimal getRating() { return rating; }
    public int getDeliveryTimeMinutes() { return deliveryTimeMinutes; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public boolean isOpen() { return open; }
    public boolean isFeatured() { return featured; }
}
