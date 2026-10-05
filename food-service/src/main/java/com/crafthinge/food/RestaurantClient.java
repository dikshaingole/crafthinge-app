package com.crafthinge.food;

import com.crafthinge.food.MenuDtos.RestaurantRef;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Calls restaurant-service by its Kubernetes Service name
 * (http://restaurant-service:8080). Kubernetes DNS resolves it to a
 * ClusterIP that load-balances across healthy pods.
 */
@Component
public class RestaurantClient {

    private final RestClient client;

    public RestaurantClient(RestClient.Builder builder,
                            @Value("${crafthinge.services.restaurant-url}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    public RestaurantRef get(Long restaurantId) {
        return client.get().uri("/api/restaurants/{id}", restaurantId).retrieve().body(RestaurantRef.class);
    }
}
