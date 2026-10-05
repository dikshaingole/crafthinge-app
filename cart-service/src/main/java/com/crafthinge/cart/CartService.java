package com.crafthinge.cart;

import com.crafthinge.cart.CartDtos.CartView;
import com.crafthinge.common.web.ApiException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository carts;
    private final FoodClient food;

    public CartService(CartRepository carts, FoodClient food) {
        this.carts = carts;
        this.food = food;
    }

    @Transactional(readOnly = true)
    public CartView view(UUID customerId) {
        return carts.findById(customerId).map(CartView::of).orElse(CartView.empty());
    }

    /**
     * Price and name come from food-service, not from the request.
     * A cart holds one restaurant's food; switching restaurants needs replace=true.
     */
    @Transactional
    public CartView add(UUID customerId, Long menuItemId, int qty, boolean replace) {
        var item = food.item(menuItemId);
        if (item == null || !item.available()) {
            throw ApiException.badRequest("This dish is not available right now");
        }
        Cart cart = carts.findById(customerId).orElseGet(() -> new Cart(customerId));
        if (cart.getRestaurantId() != null && !cart.getRestaurantId().equals(item.restaurantId())) {
            if (!replace) {
                throw ApiException.conflict("Your cart has food from another restaurant. Clear it to add this dish.");
            }
            cart.clear();
        }
        cart.add(item.restaurantId(), item.id(), item.name(), item.emoji(), item.price(), qty);
        return CartView.of(carts.save(cart));
    }

    @Transactional
    public CartView setQuantity(UUID customerId, Long menuItemId, int qty) {
        Cart cart = carts.findById(customerId).orElseThrow(() -> ApiException.notFound("Cart"));
        cart.setQuantity(menuItemId, qty);
        return CartView.of(cart);
    }

    @Transactional
    public void clear(UUID customerId) {
        carts.findById(customerId).ifPresent(Cart::clear);
    }
}
