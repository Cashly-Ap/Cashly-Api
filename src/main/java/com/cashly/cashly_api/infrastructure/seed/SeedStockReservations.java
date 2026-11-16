package com.cashly.cashly_api.infrastructure.seed;

import com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.entities.WarehouseStockEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class SeedStockReservations {
    private SeedStockReservations() {
    }

    public static void reconcile(EntityManager entityManager) {
        var stocks = entityManager.createQuery(
                "select stock from WarehouseStockEntity stock order by stock.id",
                WarehouseStockEntity.class).getResultList();
        for (var stock : stocks) {
            entityManager.refresh(stock, LockModeType.PESSIMISTIC_WRITE);
            var orders = reserved(entityManager, stock,
                    "OrderLineAllocationEntity", "orderLine", "order");
            var invoices = reserved(entityManager, stock,
                    "InvoiceLineAllocationEntity", "invoiceLine", "invoice");
            int expected = orders.add(invoices).intValueExact();
            if (expected < 0 || expected > stock.getQuantity()) {
                throw new IllegalStateException(
                        "Draft document reservations exceed stock for product "
                                + stock.getProductId());
            }
            if (stock.getReserved() != expected) {
                stock.setReserved(expected);
                stock.setUpdatedAt(LocalDateTime.now());
            }
        }
    }

    private static BigDecimal reserved(EntityManager entityManager,
            WarehouseStockEntity stock, String allocation, String line,
            String document) {
        var total = entityManager
                .createQuery("select sum(allocation.allocated) from "
                        + allocation
                        + " allocation where allocation.warehouseId = :warehouseId"
                        + " and allocation." + line + ".productId = :productId"
                        + " and allocation." + line + "." + document
                        + ".status = :status", BigDecimal.class)
                .setParameter("warehouseId", stock.getWarehouseId())
                .setParameter("productId", stock.getProductId())
                .setParameter("status", "DRAFT").getSingleResult();
        return total == null ? BigDecimal.ZERO : total;
    }
}
