package com.crafthinge.restaurant;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public final class RestaurantDtos {
    private RestaurantDtos() { }

    public record RestaurantRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 40) String cuisine,
        @Size(max = 500) String description,
        @NotBlank String address,
        @NotBlank String city,
        @Size(max = 16) String emoji,
        @Min(5) int deliveryTimeMinutes,
        @NotNull @DecimalMin("0.00") BigDecimal deliveryFee,
        boolean open) { }

    public record RestaurantView(Long id, UUID ownerId, String name, String cuisine, String description,
                                 String address, String city, String emoji, BigDecimal rating,
                                 int deliveryTimeMinutes, BigDecimal deliveryFee, boolean open, boolean featured) {
        static RestaurantView of(Restaurant r) {
            return new RestaurantView(r.getId(), r.getOwnerId(), r.getName(), r.getCuisine(), r.getDescription(),
                r.getAddress(), r.getCity(), r.getEmoji(), r.getRating(), r.getDeliveryTimeMinutes(),
                r.getDeliveryFee(), r.isOpen(), r.isFeatured());
        }
    }
}
