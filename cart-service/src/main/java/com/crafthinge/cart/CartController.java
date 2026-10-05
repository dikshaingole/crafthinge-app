package com.crafthinge.cart;

import com.crafthinge.cart.CartDtos.AddItemRequest;
import com.crafthinge.cart.CartDtos.CartView;
import com.crafthinge.cart.CartDtos.UpdateQuantityRequest;
import com.crafthinge.common.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping
    public CartView get() {
        return service.view(AuthUser.current().id());
    }

    @PostMapping("/items")
    public CartView add(@Valid @RequestBody AddItemRequest req, @RequestParam(defaultValue = "false") boolean replace) {
        return service.add(AuthUser.current().id(), req.menuItemId(), req.quantity(), replace);
    }

    @PatchMapping("/items/{menuItemId}")
    public CartView update(@PathVariable Long menuItemId, @Valid @RequestBody UpdateQuantityRequest req) {
        return service.setQuantity(AuthUser.current().id(), menuItemId, req.quantity());
    }

    @DeleteMapping("/items/{menuItemId}")
    public CartView remove(@PathVariable Long menuItemId) {
        return service.setQuantity(AuthUser.current().id(), menuItemId, 0);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear() {
        service.clear(AuthUser.current().id());
    }
}
