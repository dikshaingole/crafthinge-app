package com.crafthinge.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.crafthinge.cart.CartDtos.MenuItemRef;
import com.crafthinge.common.web.ApiException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CartServiceTest {

    private final CartRepository carts = mock(CartRepository.class);
    private final FoodClient food = mock(FoodClient.class);
    private final CartService service = new CartService(carts, food);
    private final UUID customer = UUID.randomUUID();
    private Cart cart;

    @BeforeEach
    void setUp() {
        cart = new Cart(customer);
        when(carts.findById(customer)).thenReturn(Optional.of(cart));
        when(carts.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
        when(food.item(1L)).thenReturn(new MenuItemRef(1L, 10L, "Burger", "🍔", new BigDecimal("10.00"), true));
        when(food.item(2L)).thenReturn(new MenuItemRef(2L, 20L, "Taco", "🌮", new BigDecimal("3.00"), true));
    }

    @Test
    void addingSameItemTwiceIncreasesQuantityAndUsesServerPrice() {
        service.add(customer, 1L, 1, false);
        var view = service.add(customer, 1L, 2, false);
        assertThat(view.items()).hasSize(1);
        assertThat(view.itemCount()).isEqualTo(3);
        assertThat(view.subtotal()).isEqualByComparingTo("30.00");
    }

    @Test
    void itemFromAnotherRestaurantIsRejectedUnlessReplace() {
        service.add(customer, 1L, 1, false);
        assertThatThrownBy(() -> service.add(customer, 2L, 1, false)).isInstanceOf(ApiException.class);
        var replaced = service.add(customer, 2L, 1, true);
        assertThat(replaced.restaurantId()).isEqualTo(20L);
        assertThat(replaced.items()).hasSize(1);
    }
}
