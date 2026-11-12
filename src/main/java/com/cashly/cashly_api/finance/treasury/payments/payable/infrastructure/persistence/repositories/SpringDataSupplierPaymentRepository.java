package com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.repositories;

import com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.entities.SupplierPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SpringDataSupplierPaymentRepository
        extends JpaRepository<SupplierPaymentEntity, UUID> {

    @Query("""
            SELECT p FROM SupplierPaymentEntity p
            WHERE (:dateFrom IS NULL OR p.paymentDate >= :dateFrom)
            AND (:dateTo IS NULL OR p.paymentDate <= :dateTo)
            AND (:supplierId IS NULL OR p.supplierId = :supplierId)
            AND (:status IS NULL OR p.status = :status)
            """)
    List<SupplierPaymentEntity> findAllWithFilters(
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("supplierId") UUID supplierId,
            @Param("status") String status);
}
