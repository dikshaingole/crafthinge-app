package com.crafthinge.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** A copy of the cart line at order time: later menu price changes don't alter past orders. */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "menu_item_id")
    private Long menuItemId;
    private String name;
    private String emoji;
    @Column(name = "unit_price")
    private BigDecimal unitPrice;
    private int quantity;

    protected OrderItem() { }

    public OrderItem(Long menuItemId, String name, String emoji, BigDecimal unitPrice, int quantity) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.emoji = emoji;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public Long getMenuItemId() { return menuItemId; }
    public String getName() { return name; }
    public String getEmoji() { return emoji; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
}
