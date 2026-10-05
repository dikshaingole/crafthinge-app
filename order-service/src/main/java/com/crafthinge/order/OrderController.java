package com.crafthinge.order;

import com.crafthinge.common.security.AuthUser;
import com.crafthinge.order.OrderDtos.OrderView;
import com.crafthinge.order.OrderDtos.PlaceOrderRequest;
import com.crafthinge.order.OrderDtos.UpdateStatusRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderView place(@Valid @RequestBody PlaceOrderRequest req) {
        return service.place(AuthUser.current(), req);
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<OrderView> history() {
        return service.mine(AuthUser.current());
    }

    @GetMapping("/{id}")
    public OrderView track(@PathVariable Long id) {
        return service.get(AuthUser.current(), id);
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public List<OrderView> incoming(@PathVariable Long restaurantId) {
        return service.forRestaurant(AuthUser.current(), restaurantId);
    }

    @PatchMapping("/{id}/status")
    public OrderView updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest req) {
        return service.updateStatus(AuthUser.current(), id, req.status());
    }
}
