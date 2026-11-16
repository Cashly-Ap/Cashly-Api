package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;
import static com.cashly.cashly_api.infrastructure.seed.SeedDocuments.*;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.infrastructure.seed.SeedDocuments;
import com.cashly.cashly_api.procurement.purchases.infrastructure.persistence.entities.PurchaseEntity;
import com.cashly.cashly_api.procurement.purchases.infrastructure.persistence.entities.PurchaseLineAllocationEntity;
import com.cashly.cashly_api.procurement.purchases.infrastructure.persistence.entities.PurchaseLineEntity;
import com.cashly.cashly_api.procurement.purchases.infrastructure.persistence.entities.PurchaseNumberSequenceEntity;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseStatus;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V22PurchasesSeed implements SeedStep {

    private final EntityManager entityManager;

    public V22PurchasesSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public int version() {
        return 22;
    }

    @Override
    public void seed() {
        purchase("one", "PUR-2026-0001", id("supplier.one"),
                "Coffee Growers, C.A.", "J-20000001-1", PRODUCT_COFFEE,
                "Ground coffee", "PRD-0001", "10", "3.50",
                LocalDate.of(2026, 1, 14));
        purchase("two", "PUR-2026-0002", id("supplier.two"),
                "Spring Water, C.A.", "J-20000002-2", PRODUCT_WATER,
                "Mineral water", "PRD-0002", "24", "0.45",
                LocalDate.of(2026, 1, 14));
        for (var sample : SeedDocuments.additional()) {
            purchase(sample.key(), sample.documentNumber("PUR"),
                    sample.supplierId(), sample.supplierName(),
                    sample.supplierRif(), sample.productId(),
                    sample.productName(), sample.productCode(),
                    sample.quantity(), sample.cost(), sample.date());
            applyScenario(sample, sample.scenario());
        }
        cashPurchase("cash-one", "PUR-2026-0200", id("supplier.one"),
                "Coffee Growers, C.A.", "J-20000001-1", PRODUCT_COFFEE,
                "Ground coffee", "PRD-0001", "15", "3.50",
                LocalDate.of(2026, 1, 7));
        cashPurchase("cash-two", "PUR-2026-0201", id("supplier.two"),
                "Spring Water, C.A.", "J-20000002-2", PRODUCT_WATER,
                "Mineral water", "PRD-0002", "80", "0.45",
                LocalDate.of(2026, 1, 8));
        cashPurchase("cash-three", "PUR-2026-0202", id("supplier.one"),
                "Coffee Growers, C.A.", "J-20000001-1", PRODUCT_COFFEE,
                "Ground coffee", "PRD-0001", "20", "3.50",
                LocalDate.of(2026, 1, 9));
        for (int year : SeedDocuments.years()) {
            var sequence = entityManager.find(PurchaseNumberSequenceEntity.class,
                    year);
            int nextValue = year == 2026
                    ? Math.max(SeedDocuments.nextNumberFor(year), 203)
                    : SeedDocuments.nextNumberFor(year);
            if (sequence == null) {
                sequence = new PurchaseNumberSequenceEntity();
                sequence.setYear(year);
                sequence.setNextValue(nextValue);
                entityManager.persist(sequence);
            } else if (sequence.getNextValue() < nextValue) {
                sequence.setNextValue(nextValue);
            }
        }
    }

    private void purchase(String key, String number, UUID supplierId,
            String supplierName, String rif, UUID productId, String productName,
            String code, String quantity, String cost, LocalDate issueDate) {
        persistPurchase(key, number, supplierId, supplierName, rif, productId,
                productName, code, quantity, cost, "CREDIT", "CONFIRMED",
                issueDate, "Deterministic seed purchase");
    }

    private void cashPurchase(String key, String number, UUID supplierId,
            String supplierName, String rif, UUID productId, String productName,
            String code, String quantity, String cost, LocalDate issueDate) {
        persistPurchase(key, number, supplierId, supplierName, rif, productId,
                productName, code, quantity, cost, "CASH", "CONFIRMED",
                issueDate, "Cash seed purchase");
    }

    private void persistPurchase(String key, String number, UUID supplierId,
            String supplierName, String rif, UUID productId, String productName,
            String code, String quantity, String cost, String paymentType,
            String status, LocalDate issueDate, String notes) {
        var purchaseId = id("purchase." + key);
        if (entityManager.find(PurchaseEntity.class, purchaseId) != null) {
            return;
        }
        BigDecimal qty = new BigDecimal(quantity),
                unitCost = new BigDecimal(cost), total = qty.multiply(unitCost);
        var purchase = new PurchaseEntity();
        purchase.setId(purchaseId);
        purchase.setPurchaseNumber(number);
        purchase.setSupplierId(supplierId);
        purchase.setSupplierName(supplierName);
        purchase.setSupplierRif(rif);
        purchase.setCreatedBy(ADMIN_USER);
        purchase.setCreatedByName("Cashly Administrator");
        purchase.setStatus(status);
        purchase.setPaymentType(paymentType);
        purchase.setNotes(notes);
        purchase.setSubtotal(total);
        purchase.setReturnedTotal(BigDecimal.ZERO);
        purchase.setIssueDate(issueDate);
        purchase.setDueDate(issueDate.plusDays(30));
        purchase.setConfirmedAt(issueDate.atTime(8, 0));
        purchase.setCreatedAt(issueDate.atTime(8, 0));
        purchase.setUpdatedAt(issueDate.atTime(8, 0));
        var line = new PurchaseLineEntity();
        line.setId(id("purchase-line." + key));
        line.setPurchase(purchase);
        line.setProductId(productId);
        line.setProductName(productName);
        line.setProductCode(code);
        line.setQuantity(qty);
        line.setUnitCost(unitCost);
        line.setLineTotal(total);
        var allocation = new PurchaseLineAllocationEntity();
        allocation.setId(id("purchase-allocation." + key));
        allocation.setPurchaseLine(line);
        allocation.setWarehouseId(WAREHOUSE_MAIN);
        allocation.setQuantity(qty);
        line.getAllocations().add(allocation);
        purchase.getLines().add(line);
        entityManager.persist(purchase);
    }

    private void applyScenario(SeedDocuments.Sample sample, Scenario scenario) {
        var purchase = entityManager.find(PurchaseEntity.class,
                id("purchase." + sample.key()));
        if (purchase == null
                || !"Deterministic seed purchase".equals(purchase.getNotes())) {
            return;
        }
        purchase.setStatus(scenario.purchase().name());
        purchase.setConfirmedAt(scenario.purchase() == PurchaseStatus.CONFIRMED
                ? sample.createdAt(9, 0)
                : null);
        purchase.setCancelledAt(scenario.purchase() == PurchaseStatus.CANCELLED
                ? sample.createdAt(10, 0)
                : null);
        purchase.setCancelledReason(
                scenario.purchase() == PurchaseStatus.CANCELLED
                        ? CANCELLATION_REASON
                        : null);
        purchase.setNotes(SCENARIO_NOTES);
        purchase.setUpdatedAt(sample.createdAt(10, 0));
    }

}
