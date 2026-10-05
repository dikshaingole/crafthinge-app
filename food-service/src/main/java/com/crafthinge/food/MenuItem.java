package com.crafthinge.food;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private String emoji;
    private boolean vegetarian;
    private boolean spicy;
    private boolean available = true;
    private boolean featured;

    protected MenuItem() { }

    public MenuItem(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public void apply(MenuDtos.MenuItemRequest r) {
        this.name = r.name();
        this.description = r.description();
        this.category = r.category();
        this.price = r.price();
        this.emoji = r.emoji();
        this.vegetarian = r.vegetarian();
        this.spicy = r.spicy();
        this.available = r.available();
    }

    public Long getId() { return id; }
    public Long getRestaurantId() { return restaurantId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public String getEmoji() { return emoji; }
    public boolean isVegetarian() { return vegetarian; }
    public boolean isSpicy() { return spicy; }
    public boolean isAvailable() { return available; }
    public boolean isFeatured() { return featured; }
}
