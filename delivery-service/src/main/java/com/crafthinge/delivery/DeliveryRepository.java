package com.crafthinge.delivery;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    boolean existsByOrderId(Long orderId);

    List<Delivery> findByStatusOrderByCreatedAtAsc(Delivery.Status status);

    List<Delivery> findByPartnerIdOrderByUpdatedAtDesc(UUID partnerId);

    /**
     * Atomic claim: two drivers tapping "Accept" at the same moment cannot both
     * win, because only one UPDATE matches "partner_id is null".
     */
    @Modifying(clearAutomatically = true)
    @Query("""
        update Delivery d set d.partnerId = :partnerId, d.status = :assigned, d.updatedAt = CURRENT_TIMESTAMP
        where d.id = :id and d.partnerId is null and d.status = :awaiting""")
    int claim(Long id, UUID partnerId, Delivery.Status assigned, Delivery.Status awaiting);
}
