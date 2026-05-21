package com.parsun.parsun_api.finances.domain.repositories;

import com.parsun.parsun_api.finances.domain.entities.PaymentAllocation;

import java.util.List;
import java.util.UUID;

public interface PaymentAllocationRepository {
    PaymentAllocation save(PaymentAllocation allocation);
    List<PaymentAllocation> findByPaymentId(UUID paymentId);
    List<PaymentAllocation> findByInvoiceId(UUID invoiceId);
    void deleteByPaymentId(UUID paymentId);
}
