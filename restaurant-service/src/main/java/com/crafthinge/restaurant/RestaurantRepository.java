package com.crafthinge.restaurant;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByOwnerIdOrderByName(UUID ownerId);

    @Query("""
        select r from Restaurant r
        where (:cuisine = '' or lower(r.cuisine) = lower(:cuisine))
          and (:q = '' or lower(r.name) like lower(concat('%', :q, '%')))
        order by r.featured desc, r.rating desc""")
    List<Restaurant> search(String cuisine, String q);

    /** Interface projection: Spring Data maps the aliases onto the getters. */
    interface CuisineCount {
        String getCuisine();
        long getRestaurants();
    }

    @Query("select r.cuisine as cuisine, count(r) as restaurants from Restaurant r group by r.cuisine order by r.cuisine")
    List<CuisineCount> cuisines();
}
