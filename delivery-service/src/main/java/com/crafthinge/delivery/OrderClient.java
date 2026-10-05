package com.crafthinge.delivery;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Tells order-service about pickup/delivery (with the driver's forwarded token). */
@Component
public class OrderClient {

    private final RestClient client;

    public OrderClient(RestClient.Builder builder, @Value("${crafthinge.services.order-url}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    public void updateStatus(Long orderId, String status) {
        client.patch().uri("/api/orders/{id}/status", orderId).body(Map.of("status", status))
            .retrieve().toBodilessEntity();
    }
}
