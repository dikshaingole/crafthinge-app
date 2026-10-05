package com.crafthinge.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.crafthinge.common.security.AuthUser;
import com.crafthinge.common.web.ApiException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RestaurantServiceTest {

    private final RestaurantRepository repo = mock(RestaurantRepository.class);
    private final RestaurantService service = new RestaurantService(repo);
    private final UUID ownerId = UUID.randomUUID();

    @Test
    void ownerCanAccessOwnRestaurant() {
        when(repo.findById(1L)).thenReturn(Optional.of(new Restaurant(ownerId)));
        assertThat(service.ownedBy(new AuthUser(ownerId, "o@x.com", List.of("RESTAURANT_OWNER")), 1L)).isNotNull();
    }

    @Test
    void otherOwnerIsForbidden() {
        when(repo.findById(1L)).thenReturn(Optional.of(new Restaurant(ownerId)));
        var stranger = new AuthUser(UUID.randomUUID(), "s@x.com", List.of("RESTAURANT_OWNER"));
        assertThatThrownBy(() -> service.ownedBy(stranger, 1L)).isInstanceOf(ApiException.class);
    }

    @Test
    void missingRestaurantIsNotFound() {
        when(repo.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(9L)).hasMessageContaining("not found");
    }
}
