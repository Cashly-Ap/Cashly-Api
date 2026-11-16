package com.cashly.cashly_api.finance.treasury.payments.payable.domain.repository;

import com.cashly.cashly_api.finance.treasury.payments.payable.domain.entity.SupplierPaymentAllocation;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface SupplierPaymentAllocationRepository {
    SupplierPaymentAllocation save(SupplierPaymentAllocation allocation);
    List<SupplierPaymentAllocation> findByPaymentId(UUID paymentId);
    List<SupplierPaymentAllocation> findByPurchaseId(UUID purchaseId);
    BigDecimal getAllocatedAmountByPurchaseId(UUID purchaseId);
    void deleteByPaymentId(UUID paymentId);
}
