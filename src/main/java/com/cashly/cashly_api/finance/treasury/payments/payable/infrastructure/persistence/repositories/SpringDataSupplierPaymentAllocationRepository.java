package com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.repositories;

import com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.entities.SupplierPaymentAllocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface SpringDataSupplierPaymentAllocationRepository
        extends JpaRepository<SupplierPaymentAllocationEntity, UUID> {

    List<SupplierPaymentAllocationEntity> findByPaymentId(UUID paymentId);
    List<SupplierPaymentAllocationEntity> findByPurchaseId(UUID purchaseId);
    void deleteByPaymentId(UUID paymentId);

    @Query("SELECT COALESCE(SUM(a.allocatedAmount), 0) FROM SupplierPaymentAllocationEntity a WHERE a.purchaseId = :purchaseId")
    BigDecimal sumAllocatedAmountByPurchaseId(
            @Param("purchaseId") UUID purchaseId);
}
