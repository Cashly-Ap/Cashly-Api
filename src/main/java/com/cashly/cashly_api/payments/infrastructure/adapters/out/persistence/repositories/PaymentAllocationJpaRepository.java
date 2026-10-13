package com.cashly.cashly_api.payments.infrastructure.adapters.out.persistence.repositories;

import com.cashly.cashly_api.payments.infrastructure.adapters.out.persistence.entities.PaymentAllocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentAllocationJpaRepository extends JpaRepository<PaymentAllocationEntity, UUID> {

    List<PaymentAllocationEntity> findByPaymentId(UUID paymentId);
    List<PaymentAllocationEntity> findByInvoiceId(UUID invoiceId);
    void deleteByPaymentId(UUID paymentId);

    @Query("SELECT a.invoiceId, SUM(a.allocatedAmount) FROM PaymentAllocationEntity a WHERE a.invoiceId IN :invoiceIds GROUP BY a.invoiceId")
    List<Object[]> sumByInvoiceIds(@Param("invoiceIds") List<UUID> invoiceIds);

    @Query("SELECT COALESCE(SUM(a.allocatedAmount), 0) FROM PaymentAllocationEntity a WHERE a.invoiceId = :invoiceId")
    BigDecimal sumAllocatedAmountByInvoiceId(@Param("invoiceId") UUID invoiceId);
}
