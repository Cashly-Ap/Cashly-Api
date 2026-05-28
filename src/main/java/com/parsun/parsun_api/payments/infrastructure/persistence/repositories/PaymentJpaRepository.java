package com.parsun.parsun_api.payments.infrastructure.persistence.repositories;

import com.parsun.parsun_api.payments.infrastructure.persistence.entities.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {

    @Query("""
            SELECT p FROM PaymentEntity p
            WHERE (:dateFrom IS NULL OR p.paymentDate >= :dateFrom)
            AND (:dateTo IS NULL OR p.paymentDate <= :dateTo)
            AND (:customerId IS NULL OR p.customerId = :customerId)
            AND (:type IS NULL OR p.type = :type)
            AND (:status IS NULL OR p.status = :status)
            """)
    List<PaymentEntity> findAllWithFilters(
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("customerId") UUID customerId,
            @Param("type") String type,
            @Param("status") String status
    );
}
