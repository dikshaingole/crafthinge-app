package com.crafthinge.food;

import com.crafthinge.common.security.AuthUser;
import com.crafthinge.food.MenuDtos.MenuItemRequest;
import com.crafthinge.food.MenuDtos.MenuItemView;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService service;
    private final MenuItemRepository repo;

    public MenuController(MenuService service, MenuItemRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @GetMapping("/restaurants/{restaurantId}/items")
    public List<MenuItemView> menu(@PathVariable Long restaurantId) {
        return repo.findByRestaurantIdOrderByCategoryAscNameAsc(restaurantId).stream().map(MenuItemView::of).toList();
    }

    /** "Fresh picks" on the home page. */
    @GetMapping("/featured")
    public List<MenuItemView> featured() {
        return repo.findTop12ByFeaturedTrueAndAvailableTrueOrderByIdAsc().stream().map(MenuItemView::of).toList();
    }

    /** Used by cart-service to get the real price (never trust a price sent by the browser). */
    @GetMapping("/items/{id}")
    public MenuItemView item(@PathVariable Long id) {
        return MenuItemView.of(service.get(id));
    }

    @PostMapping("/restaurants/{restaurantId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public MenuItemView create(@PathVariable Long restaurantId, @Valid @RequestBody MenuItemRequest req) {
        return MenuItemView.of(service.create(AuthUser.current(), restaurantId, req));
    }

    @PutMapping("/items/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public MenuItemView update(@PathVariable Long id, @Valid @RequestBody MenuItemRequest req) {
        return MenuItemView.of(service.update(AuthUser.current(), id, req));
    }

    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public void delete(@PathVariable Long id) {
        service.delete(AuthUser.current(), id);
    }
}
