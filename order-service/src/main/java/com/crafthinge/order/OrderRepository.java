package com.crafthinge.order;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findTop50ByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    List<Order> findTop100ByRestaurantIdAndStatusNotInOrderByCreatedAtDesc(Long restaurantId, Collection<OrderStatus> excluded);
}
