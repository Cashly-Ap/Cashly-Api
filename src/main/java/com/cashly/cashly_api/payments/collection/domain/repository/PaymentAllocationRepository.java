package com.cashly.cashly_api.payments.collection.domain.repository;

import com.cashly.cashly_api.payments.collection.domain.entity.PaymentAllocation;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PaymentAllocationRepository {
    PaymentAllocation save(PaymentAllocation allocation);
    List<PaymentAllocation> findByPaymentId(UUID paymentId);
    List<PaymentAllocation> findByInvoiceId(UUID invoiceId);
    void deleteByPaymentId(UUID paymentId);
    BigDecimal getAllocatedAmountByInvoiceId(UUID invoiceId);
    Map<UUID, BigDecimal> sumByInvoiceIds(Collection<UUID> invoiceIds);
}
