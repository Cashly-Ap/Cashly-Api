package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.procurement.purchases.infrastructure.persistence.entities.PurchaseEntity;
import com.cashly.cashly_api.procurement.purchases.infrastructure.persistence.entities.PurchaseLineAllocationEntity;
import com.cashly.cashly_api.procurement.purchases.infrastructure.persistence.entities.PurchaseLineEntity;
import com.cashly.cashly_api.procurement.purchases.infrastructure.persistence.entities.PurchaseNumberSequenceEntity;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
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
        purchase("one", "PUR-2025-0001", id("supplier.one"), "Coffee Growers, C.A.", "J-20000001-1", PRODUCT_COFFEE, "Ground coffee", "PRD-0001", "10", "3.50");
        purchase("two", "PUR-2025-0002", id("supplier.two"), "Spring Water, C.A.", "J-20000002-2", PRODUCT_WATER, "Mineral water", "PRD-0002", "24", "0.45");
        if (entityManager.find(PurchaseNumberSequenceEntity.class, 2025) == null) {
            var s = new PurchaseNumberSequenceEntity();
            s.setYear(2025);
            s.setNextValue(3);
            entityManager.persist(s);
        }
    }

    private void purchase(String key, String number, UUID supplierId, String supplierName, String rif, UUID productId,
            String productName, String code, String quantity, String cost) {
        var purchaseId = id("purchase." + key);
        if (entityManager.find(PurchaseEntity.class, purchaseId) != null) {
            return;
        }
        BigDecimal qty = new BigDecimal(quantity), unitCost = new BigDecimal(cost), total = qty.multiply(unitCost);
        var purchase = new PurchaseEntity();
        purchase.setId(purchaseId);
        purchase.setPurchaseNumber(number);
        purchase.setSupplierId(supplierId);
        purchase.setSupplierName(supplierName);
        purchase.setSupplierRif(rif);
        purchase.setCreatedBy(ADMIN_USER);
        purchase.setCreatedByName("Cashly Administrator");
        purchase.setStatus("CONFIRMED");
        purchase.setNotes("Deterministic seed purchase");
        purchase.setSubtotal(total);
        purchase.setConfirmedAt(LocalDateTime.of(2025, 1, 14, 8, 0));
        purchase.setCreatedAt(LocalDateTime.of(2025, 1, 14, 8, 0));
        purchase.setUpdatedAt(LocalDateTime.of(2025, 1, 14, 8, 0));
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
}
