package com.cashly.cashly_api.procurement.purchases.infrastructure.integration;

import com.cashly.cashly_api.procurement.purchases.application.ports.out.SupplierCreditGateway;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class LocalSupplierCreditGateway implements SupplierCreditGateway {

    private static final String DEBT_QUERY = """
            SELECT COALESCE(SUM(p.subtotal - COALESCE(p.returned_total, 0)
                       - COALESCE(pa.paid, 0)), 0)
            FROM purchases p
            LEFT JOIN (
                SELECT purchase_id, SUM(allocated_amount) AS paid
                FROM supplier_payment_allocations
                GROUP BY purchase_id
            ) pa ON pa.purchase_id = p.id
            WHERE p.supplier_id = :supplierId
            AND p.status NOT IN ('DRAFT', 'CANCELLED', 'PAID',
                                 'FULLY_RETURNED')
            AND (p.subtotal - COALESCE(p.returned_total, 0)
                 - COALESCE(pa.paid, 0)) > 0
            """;

    private final EntityManager entityManager;

    public LocalSupplierCreditGateway(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public boolean isCreditAvailable(UUID supplierId, BigDecimal amount) {
        BigDecimal requested = amount != null ? amount : BigDecimal.ZERO;
        return getAvailableCredit(supplierId).compareTo(requested) >= 0;
    }

    @Override
    public BigDecimal getAvailableCredit(UUID supplierId) {
        BigDecimal limit = number(entityManager.createNativeQuery(
                "SELECT COALESCE(credit_limit, 0) FROM suppliers WHERE id = :supplierId")
                .setParameter("supplierId", supplierId).getResultList());
        return limit.subtract(getTotalDebt(supplierId))
                .max(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getTotalDebt(UUID supplierId) {
        return number(entityManager.createNativeQuery(DEBT_QUERY)
                .setParameter("supplierId", supplierId).getResultList());
    }

    @Override
    public boolean hasOverduePurchases(UUID supplierId) {
        List<?> result = entityManager.createNativeQuery("""
                SELECT COUNT(*)
                FROM purchases p
                LEFT JOIN (
                    SELECT purchase_id, SUM(allocated_amount) AS paid
                    FROM supplier_payment_allocations
                    GROUP BY purchase_id
                ) pa ON pa.purchase_id = p.id
                WHERE p.supplier_id = :supplierId
                AND p.due_date < CURRENT_DATE
                AND p.status NOT IN ('DRAFT', 'CANCELLED', 'PAID',
                                     'FULLY_RETURNED')
                AND (p.subtotal - COALESCE(p.returned_total, 0)
                     - COALESCE(pa.paid, 0)) > 0
                """).setParameter("supplierId", supplierId).getResultList();
        return result.isEmpty()
                ? false
                : ((Number) result.get(0)).longValue() > 0;
    }

    private BigDecimal number(List<?> result) {
        if (result.isEmpty() || result.get(0) == null) {
            return BigDecimal.ZERO;
        }
        Object value = result.get(0);
        return value instanceof BigDecimal decimal ? decimal
                : new BigDecimal(value.toString());
    }
}
