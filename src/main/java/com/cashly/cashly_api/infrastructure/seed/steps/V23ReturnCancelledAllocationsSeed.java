package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnCancelledAllocationEntity;
import com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnEntity;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class V23ReturnCancelledAllocationsSeed implements SeedStep {
    private final EntityManager entityManager;
    public V23ReturnCancelledAllocationsSeed(EntityManager entityManager) { this.entityManager = entityManager; }
    @Override public int version() { return 23; }
    @Override public void seed() {
        allocation("one", RETURN_ONE, PAYMENT_ONE, INVOICE_ONE, "1.50");
        allocation("two", RETURN_TWO, PAYMENT_TWO, INVOICE_TWO, "0.50");
    }
    private void allocation(String key, java.util.UUID returnId, java.util.UUID paymentId, java.util.UUID invoiceId, String amount) {
        var allocationId = id("return-cancelled-allocation." + key); if (entityManager.find(ReturnCancelledAllocationEntity.class, allocationId) != null) return;
        var allocation = new ReturnCancelledAllocationEntity(); allocation.setId(allocationId); allocation.setReturnEntity(entityManager.getReference(ReturnEntity.class, returnId));
        allocation.setPaymentId(paymentId); allocation.setInvoiceId(invoiceId); allocation.setAmount(new BigDecimal(amount)); entityManager.persist(allocation);
    }
}
