package com.cashly.cashly_api.finance.treasury.payments.payable.application.ports.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayablePurchaseGateway {

    List<PayablePurchase> findPayableBySupplier(UUID supplierId);

    Optional<PayablePurchase> findById(UUID purchaseId);

    void applyPaidTotal(UUID purchaseId, BigDecimal totalPaid);

    record PayablePurchase(UUID id, UUID supplierId, BigDecimal subtotal,
            BigDecimal returnedTotal) {

        public BigDecimal balanceTotal() {
            BigDecimal returned = returnedTotal != null ? returnedTotal
                    : BigDecimal.ZERO;
            return subtotal.subtract(returned).max(BigDecimal.ZERO);
        }
    }
}
