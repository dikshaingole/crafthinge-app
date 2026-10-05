package com.crafthinge.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public final class CartDtos {
    private CartDtos() { }

    public record AddItemRequest(@NotNull Long menuItemId, @Min(1) @Max(20) int quantity) { }

    public record UpdateQuantityRequest(@Min(0) @Max(20) int quantity) { }

    public record CartLine(Long menuItemId, String name, String emoji, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) { }

    public record CartView(Long restaurantId, List<CartLine> items, int itemCount, BigDecimal subtotal) {
        static CartView of(Cart cart) {
            var lines = cart.getItems().stream()
                .map(i -> new CartLine(i.getMenuItemId(), i.getName(), i.getEmoji(), i.getUnitPrice(), i.getQuantity(), i.lineTotal()))
                .toList();
            int count = lines.stream().mapToInt(CartLine::quantity).sum();
            return new CartView(cart.getRestaurantId(), lines, count, cart.subtotal());
        }

        static CartView empty() {
            return new CartView(null, List.of(), 0, BigDecimal.ZERO);
        }
    }

    /** The fields cart-service needs from food-service. */
    public record MenuItemRef(Long id, Long restaurantId, String name, String emoji, BigDecimal price, boolean available) { }
}
