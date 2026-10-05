package com.crafthinge.food;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantIdOrderByCategoryAscNameAsc(Long restaurantId);
    List<MenuItem> findTop12ByFeaturedTrueAndAvailableTrueOrderByIdAsc();
}
