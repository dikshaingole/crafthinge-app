package com.crafthinge.restaurant;

import com.crafthinge.common.security.AuthUser;
import com.crafthinge.restaurant.RestaurantRepository.CuisineCount;
import com.crafthinge.restaurant.RestaurantDtos.RestaurantRequest;
import com.crafthinge.restaurant.RestaurantDtos.RestaurantView;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService service;
    private final RestaurantRepository repo;

    public RestaurantController(RestaurantService service, RestaurantRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @GetMapping
    public List<RestaurantView> list(@RequestParam(required = false) String cuisine,
                                     @RequestParam(required = false) String q) {
        return repo.search(clean(cuisine), clean(q)).stream().map(RestaurantView::of).toList();
    }

    @GetMapping("/cuisines")
    public List<CuisineCount> cuisines() {
        return repo.cuisines();
    }

    @GetMapping("/{id}")
    public RestaurantView get(@PathVariable Long id) {
        return RestaurantView.of(service.get(id));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public List<RestaurantView> mine() {
        return repo.findByOwnerIdOrderByName(AuthUser.current().id()).stream().map(RestaurantView::of).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public RestaurantView create(@Valid @RequestBody RestaurantRequest req) {
        return RestaurantView.of(service.create(AuthUser.current(), req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public RestaurantView update(@PathVariable Long id, @Valid @RequestBody RestaurantRequest req) {
        return RestaurantView.of(service.update(AuthUser.current(), id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public void delete(@PathVariable Long id) {
        service.delete(AuthUser.current(), id);
    }

    private static String clean(String s) {
        return s == null ? "" : s.trim();
    }
}
