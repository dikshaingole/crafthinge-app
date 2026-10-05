package com.crafthinge.cart;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId;
    private String name;
    private String emoji;
    @Column(name = "unit_price")
    private BigDecimal unitPrice;
    private int quantity;

    protected CartItem() { }

    CartItem(Long menuItemId, String name, String emoji, BigDecimal unitPrice, int quantity) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.emoji = emoji;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getMenuItemId() { return menuItemId; }
    public String getName() { return name; }
    public String getEmoji() { return emoji; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    void setQuantity(int quantity) { this.quantity = quantity; }
}
