package com.crafthinge.cart;

import com.crafthinge.cart.CartDtos.MenuItemRef;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class FoodClient {

    private final RestClient client;

    public FoodClient(RestClient.Builder builder, @Value("${crafthinge.services.food-url}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    public MenuItemRef item(Long id) {
        return client.get().uri("/api/menu/items/{id}", id).retrieve().body(MenuItemRef.class);
    }
}
