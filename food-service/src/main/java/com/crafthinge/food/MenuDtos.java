package com.crafthinge.food;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public final class MenuDtos {
    private MenuDtos() { }

    public record MenuItemRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description,
        @NotBlank @Size(max = 60) String category,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @Size(max = 16) String emoji,
        boolean vegetarian,
        boolean spicy,
        boolean available) { }

    public record MenuItemView(Long id, Long restaurantId, String name, String description, String category,
                               BigDecimal price, String emoji, boolean vegetarian, boolean spicy,
                               boolean available, boolean featured) {
        static MenuItemView of(MenuItem m) {
            return new MenuItemView(m.getId(), m.getRestaurantId(), m.getName(), m.getDescription(),
                m.getCategory(), m.getPrice(), m.getEmoji(), m.isVegetarian(), m.isSpicy(),
                m.isAvailable(), m.isFeatured());
        }
    }

    /** The part of restaurant-service's response that food-service needs. */
    public record RestaurantRef(Long id, UUID ownerId, String name) { }
}
