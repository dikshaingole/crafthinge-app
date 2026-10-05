package com.crafthinge.food;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.crafthinge.common.security.AuthUser;
import com.crafthinge.common.web.ApiException;
import com.crafthinge.food.MenuDtos.RestaurantRef;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MenuServiceTest {

    private final RestaurantClient restaurants = mock(RestaurantClient.class);
    private final MenuService service = new MenuService(mock(MenuItemRepository.class), restaurants);
    private final UUID ownerId = UUID.randomUUID();

    @Test
    void ownerPassesOwnershipCheck() {
        when(restaurants.get(1L)).thenReturn(new RestaurantRef(1L, ownerId, "R"));
        assertThatCode(() -> service.requireOwner(new AuthUser(ownerId, "o@x", List.of()), 1L)).doesNotThrowAnyException();
    }

    @Test
    void nonOwnerCannotEditMenu() {
        when(restaurants.get(1L)).thenReturn(new RestaurantRef(1L, ownerId, "R"));
        assertThatThrownBy(() -> service.requireOwner(new AuthUser(UUID.randomUUID(), "x@x", List.of()), 1L))
            .isInstanceOf(ApiException.class);
    }
}
