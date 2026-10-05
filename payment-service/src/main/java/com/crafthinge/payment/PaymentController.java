package com.crafthinge.payment;

import com.crafthinge.common.security.AuthUser;
import com.crafthinge.payment.PaymentDtos.ChargeRequest;
import com.crafthinge.payment.PaymentDtos.PaymentView;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;
    private final PaymentRepository repo;

    public PaymentController(PaymentService service, PaymentRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    /** Called by order-service with the customer's forwarded token. */
    @PostMapping
    public PaymentView charge(@Valid @RequestBody ChargeRequest req) {
        return PaymentView.of(service.charge(AuthUser.current().id(), req));
    }

    @GetMapping("/order/{orderId}")
    public List<PaymentView> forOrder(@PathVariable Long orderId) {
        var me = AuthUser.current().id();
        return repo.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
            .filter(p -> p.getCustomerId().equals(me))
            .map(PaymentView::of).toList();
    }
}
