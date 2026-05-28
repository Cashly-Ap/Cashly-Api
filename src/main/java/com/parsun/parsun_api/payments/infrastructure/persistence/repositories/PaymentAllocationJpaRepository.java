package com.parsun.parsun_api.payments.infrastructure.persistence.repositories;

import com.parsun.parsun_api.payments.infrastructure.persistence.entities.PaymentAllocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentAllocationJpaRepository extends JpaRepository<PaymentAllocationEntity, UUID> {

    List<PaymentAllocationEntity> findByPaymentId(UUID paymentId);
    List<PaymentAllocationEntity> findByInvoiceId(UUID invoiceId);
    void deleteByPaymentId(UUID paymentId);
}
