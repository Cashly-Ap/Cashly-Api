package com.parsun.parsun_api.payments.domain.repositories;

import com.parsun.parsun_api.payments.domain.entities.PaymentAllocation;

import java.util.List;
import java.util.UUID;

public interface PaymentAllocationRepository {
    PaymentAllocation save(PaymentAllocation allocation);
    List<PaymentAllocation> findByPaymentId(UUID paymentId);
    List<PaymentAllocation> findByInvoiceId(UUID invoiceId);
    void deleteByPaymentId(UUID paymentId);
}
