package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnEntity;
import com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnLineAllocationEntity;
import com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnLineEntity;
import com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnNumberSequenceEntity;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V17ReturnsSeed implements SeedStep {
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2025, 1, 11, 8, 0);
    private final EntityManager entityManager;
    public V17ReturnsSeed(EntityManager entityManager) { this.entityManager = entityManager; }
    @Override public int version() { return 17; }
    @Override public void seed() {
        returned(RETURN_ONE, "RET-2025-0001", INVOICE_ONE, CUSTOMER_ONE, "one", "invoice-line.one", PRODUCT_COFFEE, "1", "6.50");
        returned(RETURN_TWO, "RET-2025-0002", INVOICE_TWO, CUSTOMER_TWO, "two", "invoice-line.two", PRODUCT_WATER, "1", "1.00");
        if (entityManager.find(ReturnNumberSequenceEntity.class, 2025) == null) {
            var sequence = new ReturnNumberSequenceEntity(); sequence.setYear(2025); sequence.setNextValue(3); entityManager.persist(sequence);
        }
    }
    private void returned(UUID id, String number, UUID invoiceId, UUID customerId, String key, String invoiceLineKey,
                          UUID productId, String quantity, String price) {
        if (entityManager.find(ReturnEntity.class, id) != null) return;
        BigDecimal qty = new BigDecimal(quantity), unitPrice = new BigDecimal(price), total = qty.multiply(unitPrice);
        var returned = new ReturnEntity(); returned.setId(id); returned.setReturnNumber(number); returned.setInvoiceId(invoiceId);
        returned.setCustomerId(customerId); returned.setReturnType("MANUAL"); returned.setReason("Seed quality return"); returned.setTotal(total);
        returned.setCreatedBy(ADMIN_USER); returned.setCreatedByName("Cashly Administrator"); returned.setCreatedAt(CREATED_AT); returned.setUpdatedAt(CREATED_AT);
        var line = new ReturnLineEntity(); line.setId(id("return-line." + key)); line.setReturnEntity(returned); line.setInvoiceLineId(id(invoiceLineKey));
        line.setProductId(productId); line.setQuantity(qty); line.setUnitPrice(unitPrice); line.setLineTotal(total);
        var allocation = new ReturnLineAllocationEntity(); allocation.setId(id("return-allocation." + key)); allocation.setReturnLine(line);
        allocation.setWarehouseId(WAREHOUSE_MAIN); allocation.setAllocated(qty); line.getAllocations().add(allocation);
        returned.getLines().add(line); entityManager.persist(returned);
    }
}
