package com.cashly.cashly_api.procurement.purchases.infrastructure.integration;

import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchasePaymentQuery;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class LocalPurchasePaymentQuery implements PurchasePaymentQuery {

    private final EntityManager entityManager;

    public LocalPurchasePaymentQuery(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public BigDecimal findPaidAmount(UUID purchaseId) {
        List<?> result = entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(allocated_amount), 0) FROM supplier_payment_allocations WHERE purchase_id = :purchaseId")
                .setParameter("purchaseId", purchaseId).getResultList();
        if (result.isEmpty() || result.get(0) == null) {
            return BigDecimal.ZERO;
        }
        Object value = result.get(0);
        return value instanceof BigDecimal decimal ? decimal
                : new BigDecimal(value.toString());
    }
}
