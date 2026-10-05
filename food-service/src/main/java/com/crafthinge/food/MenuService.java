package com.crafthinge.food;

import com.crafthinge.common.security.AuthUser;
import com.crafthinge.common.web.ApiException;
import com.crafthinge.food.MenuDtos.MenuItemRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuService {

    private final MenuItemRepository repo;
    private final RestaurantClient restaurants;

    public MenuService(MenuItemRepository repo, RestaurantClient restaurants) {
        this.repo = repo;
        this.restaurants = restaurants;
    }

    public MenuItem get(Long id) {
        return repo.findById(id).orElseThrow(() -> ApiException.notFound("Menu item " + id));
    }

    @Transactional
    public MenuItem create(AuthUser user, Long restaurantId, MenuItemRequest req) {
        requireOwner(user, restaurantId);
        MenuItem item = new MenuItem(restaurantId);
        item.apply(req);
        return repo.save(item);
    }

    @Transactional
    public MenuItem update(AuthUser user, Long itemId, MenuItemRequest req) {
        MenuItem item = get(itemId);
        requireOwner(user, item.getRestaurantId());
        item.apply(req);
        return item;
    }

    @Transactional
    public void delete(AuthUser user, Long itemId) {
        MenuItem item = get(itemId);
        requireOwner(user, item.getRestaurantId());
        repo.delete(item);
    }

    /** Ownership lives in restaurant-service, so we ask it (database-per-service). */
    void requireOwner(AuthUser user, Long restaurantId) {
        var restaurant = restaurants.get(restaurantId);
        if (restaurant == null || !user.id().equals(restaurant.ownerId())) {
            throw ApiException.forbidden("You do not own restaurant " + restaurantId);
        }
    }
}
