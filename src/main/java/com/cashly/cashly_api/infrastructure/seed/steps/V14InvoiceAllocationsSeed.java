package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.entities.InvoiceLineAllocationEntity;
import com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.entities.InvoiceLineEntity;
import com.cashly.cashly_api.infrastructure.seed.SeedReferences;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.infrastructure.seed.SeedDocuments;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class V14InvoiceAllocationsSeed implements SeedStep {
    private final EntityManager entityManager;
    public V14InvoiceAllocationsSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    public int version() {
        return 14;
    }
    @Override
    public void seed() {
        allocation("one", "invoice-line.one", "2");
        allocation("two", "invoice-line.two", "3");
        for (var sample : SeedDocuments.additional()) {
            allocation(sample.key(), "invoice-line." + sample.key(),
                    sample.quantity());
        }
    }
    private void allocation(String key, String lineKey, String quantity) {
        var allocationId = id("invoice-allocation." + key);
        if (entityManager.find(InvoiceLineAllocationEntity.class,
                allocationId) != null)
            return;
        var line = SeedReferences.findOrWarn(entityManager,
                InvoiceLineEntity.class, id(lineKey), "invoice line " + lineKey);
        if (line == null)
            return;
        var allocation = new InvoiceLineAllocationEntity();
        allocation.setId(allocationId);
        allocation.setInvoiceLine(line);
        allocation.setWarehouseId(WAREHOUSE_MAIN);
        allocation.setAllocated(new BigDecimal(quantity));
        entityManager.persist(allocation);
    }
}
