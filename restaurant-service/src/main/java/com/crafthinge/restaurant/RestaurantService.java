package com.crafthinge.restaurant;

import com.crafthinge.common.security.AuthUser;
import com.crafthinge.common.web.ApiException;
import com.crafthinge.restaurant.RestaurantDtos.RestaurantRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestaurantService {

    private final RestaurantRepository repo;

    public RestaurantService(RestaurantRepository repo) {
        this.repo = repo;
    }

    public Restaurant get(Long id) {
        return repo.findById(id).orElseThrow(() -> ApiException.notFound("Restaurant " + id));
    }

    @Transactional
    public Restaurant create(AuthUser user, RestaurantRequest req) {
        Restaurant r = new Restaurant(user.id());
        r.apply(req);
        return repo.save(r);
    }

    @Transactional
    public Restaurant update(AuthUser user, Long id, RestaurantRequest req) {
        Restaurant r = ownedBy(user, id);
        r.apply(req);
        return r;
    }

    @Transactional
    public void delete(AuthUser user, Long id) {
        repo.delete(ownedBy(user, id));
    }

    /** Owners can only change their own restaurants. */
    Restaurant ownedBy(AuthUser user, Long id) {
        Restaurant r = get(id);
        if (!r.getOwnerId().equals(user.id())) {
            throw ApiException.forbidden("You do not own restaurant " + id);
        }
        return r;
    }
}
