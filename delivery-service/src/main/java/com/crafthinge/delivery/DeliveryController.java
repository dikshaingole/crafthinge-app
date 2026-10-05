package com.crafthinge.delivery;

import com.crafthinge.common.security.AuthUser;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService service;
    private final DeliveryRepository repo;

    public DeliveryController(DeliveryService service, DeliveryRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @GetMapping("/available")
    public List<Delivery> available() {
        return repo.findByStatusOrderByCreatedAtAsc(Delivery.Status.AWAITING_PARTNER);
    }

    @GetMapping("/mine")
    public List<Delivery> mine() {
        return repo.findByPartnerIdOrderByUpdatedAtDesc(AuthUser.current().id());
    }

    @PostMapping("/{id}/accept")
    public Delivery accept(@PathVariable Long id) {
        return service.accept(AuthUser.current(), id);
    }

    @PostMapping("/{id}/pickup")
    public Delivery pickUp(@PathVariable Long id) {
        return service.pickUp(AuthUser.current(), id);
    }

    @PostMapping("/{id}/deliver")
    public Delivery deliver(@PathVariable Long id) {
        return service.deliver(AuthUser.current(), id);
    }
}
