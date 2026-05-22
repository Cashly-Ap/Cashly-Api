package com.parsun.parsun_api.finances.infrastructure.persistence.repositories;

import com.parsun.parsun_api.finances.infrastructure.persistence.entities.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {

    boolean existsByPaymentMethodId(UUID paymentMethodId);

    @Query("""
            SELECT p FROM PaymentEntity p
            WHERE (:dateFrom IS NULL OR p.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR p.createdAt <= :dateTo)
            AND (:customerId IS NULL OR p.customerId = :customerId)
            AND (:type IS NULL OR p.type = :type)
            AND (:status IS NULL OR p.status = :status)
            ORDER BY p.createdAt DESC
            """)
    List<PaymentEntity> findAllWithFilters(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("customerId") UUID customerId,
            @Param("type") String type,
            @Param("status") String status
    );
}
