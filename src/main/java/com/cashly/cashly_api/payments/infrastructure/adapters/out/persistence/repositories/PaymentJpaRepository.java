package com.cashly.cashly_api.payments.infrastructure.adapters.out.persistence.repositories;

import com.cashly.cashly_api.payments.infrastructure.adapters.out.persistence.entities.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {

    @Query("""
            SELECT p FROM PaymentEntity p
            WHERE (:dateFrom IS NULL OR p.paymentDate >= :dateFrom)
            AND (:dateTo IS NULL OR p.paymentDate <= :dateTo)
            AND (:customerId IS NULL OR p.customerId = :customerId)
            AND (:status IS NULL OR p.status = :status)
            """)
    List<PaymentEntity> findAllWithFilters(
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("customerId") UUID customerId,
            @Param("status") String status
    );

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM PaymentEntity p
            WHERE p.customerId = :customerId
            AND p.status = 'APPLIED'
            """)
    BigDecimal getRefundableAmountByCustomerId(@Param("customerId") UUID customerId);

    long countByPaymentMethodId(UUID paymentMethodId);
}
