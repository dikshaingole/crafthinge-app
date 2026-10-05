package com.crafthinge.cart;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @Column(name = "customer_id")
    private UUID customerId;
    @Column(name = "restaurant_id")
    private Long restaurantId;
    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "customer_id", nullable = false)
    private List<CartItem> items = new ArrayList<>();

    protected Cart() { }

    public Cart(UUID customerId) {
        this.customerId = customerId;
    }

    public Optional<CartItem> find(Long menuItemId) {
        return items.stream().filter(i -> i.getMenuItemId().equals(menuItemId)).findFirst();
    }

    public void add(Long restaurantId, Long menuItemId, String name, String emoji, BigDecimal unitPrice, int qty) {
        this.restaurantId = restaurantId;
        find(menuItemId).ifPresentOrElse(
            i -> i.setQuantity(i.getQuantity() + qty),
            () -> items.add(new CartItem(menuItemId, name, emoji, unitPrice, qty)));
        touch();
    }

    public void setQuantity(Long menuItemId, int qty) {
        if (qty <= 0) {
            items.removeIf(i -> i.getMenuItemId().equals(menuItemId));
        } else {
            find(menuItemId).ifPresent(i -> i.setQuantity(qty));
        }
        if (items.isEmpty()) {
            restaurantId = null;
        }
        touch();
    }

    public void clear() {
        items.clear();
        restaurantId = null;
        touch();
    }

    public BigDecimal subtotal() {
        return items.stream().map(CartItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void touch() {
        updatedAt = Instant.now();
    }

    public UUID getCustomerId() { return customerId; }
    public Long getRestaurantId() { return restaurantId; }
    public List<CartItem> getItems() { return items; }
}
