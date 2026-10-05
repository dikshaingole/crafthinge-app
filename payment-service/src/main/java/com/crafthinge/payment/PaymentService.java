package com.crafthinge.payment;

import com.crafthinge.payment.PaymentDtos.ChargeRequest;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository repo;
    private final PaymentProvider provider;
    private final MeterRegistry metrics;

    public PaymentService(PaymentRepository repo, PaymentProvider provider, MeterRegistry metrics) {
        this.repo = repo;
        this.provider = provider;
        this.metrics = metrics;
    }

    @Transactional
    public Payment charge(UUID customerId, ChargeRequest req) {
        // Idempotent: a retried request for an already-paid order returns the existing payment.
        var existing = repo.findByOrderIdAndStatus(req.orderId(), "SUCCEEDED");
        if (existing.isPresent()) {
            return existing.get();
        }
        String digits = req.cardNumber().replaceAll("\\D", "");
        String last4 = digits.length() >= 4 ? digits.substring(digits.length() - 4) : null;
        var result = provider.charge(req.amount(), "USD", digits);
        Payment payment = repo.save(new Payment(req.orderId(), customerId, req.amount(), provider.name(), result, last4));
        // Metric for dashboards/alerts (Phase 7): crafthinge_payments_total{status="FAILED"}
        metrics.counter("crafthinge.payments", "status", payment.getStatus()).increment();
        log.info("Payment {} for order {}: {}", payment.getId(), req.orderId(), payment.getStatus());
        return payment;
    }
}
