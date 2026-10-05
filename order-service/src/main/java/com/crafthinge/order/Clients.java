package com.crafthinge.order;

import com.crafthinge.order.OrderDtos.Cart;
import com.crafthinge.order.OrderDtos.Payment;
import com.crafthinge.order.OrderDtos.Restaurant;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** HTTP clients for the services order-service depends on (Kubernetes Service DNS names). */
@Component
public class Clients {

    private final RestClient cart;
    private final RestClient restaurant;
    private final RestClient payment;

    public Clients(RestClient.Builder builder,
                   @Value("${crafthinge.services.cart-url}") String cartUrl,
                   @Value("${crafthinge.services.restaurant-url}") String restaurantUrl,
                   @Value("${crafthinge.services.payment-url}") String paymentUrl) {
        this.cart = builder.clone().baseUrl(cartUrl).build();
        this.restaurant = builder.clone().baseUrl(restaurantUrl).build();
        this.payment = builder.clone().baseUrl(paymentUrl).build();
    }

    public Cart getCart() {
        return cart.get().uri("/api/cart").retrieve().body(Cart.class);
    }

    public void clearCart() {
        cart.delete().uri("/api/cart").retrieve().toBodilessEntity();
    }

    public Restaurant getRestaurant(Long id) {
        return restaurant.get().uri("/api/restaurants/{id}", id).retrieve().body(Restaurant.class);
    }

    public Payment charge(Long orderId, BigDecimal amount, String cardNumber) {
        return payment.post().uri("/api/payments")
            .body(Map.of("orderId", orderId, "amount", amount, "cardNumber", cardNumber))
            .retrieve().body(Payment.class);
    }
}
