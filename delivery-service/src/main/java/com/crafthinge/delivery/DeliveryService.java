package com.crafthinge.delivery;

import com.crafthinge.common.security.AuthUser;
import com.crafthinge.common.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryService {

    private final DeliveryRepository repo;
    private final OrderClient orders;

    public DeliveryService(DeliveryRepository repo, OrderClient orders) {
        this.repo = repo;
        this.orders = orders;
    }

    @Transactional
    public Delivery accept(AuthUser driver, Long id) {
        if (repo.claim(id, driver.id(), Delivery.Status.ASSIGNED, Delivery.Status.AWAITING_PARTNER) == 0) {
            throw ApiException.conflict("This delivery was already taken by another driver");
        }
        return repo.findById(id).orElseThrow();
    }

    public Delivery pickUp(AuthUser driver, Long id) {
        Delivery d = mine(driver, id, Delivery.Status.ASSIGNED);
        orders.updateStatus(d.getOrderId(), "OUT_FOR_DELIVERY"); // order-service validates the transition
        d.move(Delivery.Status.PICKED_UP);
        return repo.save(d);
    }

    public Delivery deliver(AuthUser driver, Long id) {
        Delivery d = mine(driver, id, Delivery.Status.PICKED_UP);
        orders.updateStatus(d.getOrderId(), "DELIVERED");
        d.move(Delivery.Status.DELIVERED);
        return repo.save(d);
    }

    private Delivery mine(AuthUser driver, Long id, Delivery.Status expected) {
        Delivery d = repo.findById(id).orElseThrow(() -> ApiException.notFound("Delivery " + id));
        if (!driver.id().equals(d.getPartnerId())) {
            throw ApiException.forbidden("This delivery is assigned to another driver");
        }
        if (d.getStatus() != expected) {
            throw ApiException.conflict("Delivery is " + d.getStatus() + ", expected " + expected);
        }
        return d;
    }
}
