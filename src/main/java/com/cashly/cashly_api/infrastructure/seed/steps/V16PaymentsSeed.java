package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.finance.treasury.payment_methods.infrastructure.persistence.entities.PaymentMethodEntity;
import com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.persistence.entities.PaymentAllocationEntity;
import com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.persistence.entities.PaymentEntity;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V16PaymentsSeed implements SeedStep {
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2025, 1,
            10, 8, 0);
    private final EntityManager entityManager;
    public V16PaymentsSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    public int version() {
        return 16;
    }
    @Override
    public void seed() {
        method("cash", "CASH", "Cash");
        method("transfer", "TRANSFER", "Bank transfer");
        payment(PAYMENT_ONE, "PAY-2025-0001", CUSTOMER_ONE, INVOICE_ONE, "cash",
                "10.00");
        payment(PAYMENT_TWO, "PAY-2025-0002", CUSTOMER_TWO, INVOICE_TWO,
                "transfer", "3.00");
    }
    private void method(String key, String code, String name) {
        UUID id = id("payment-method." + key);
        if (entityManager.find(PaymentMethodEntity.class, id) != null)
            return;
        var method = new PaymentMethodEntity();
        method.setId(id);
        method.setCode(code);
        method.setName(name);
        method.setDescription("Deterministic seed payment method");
        method.setActive(true);
        method.setCreatedAt(CREATED_AT);
        method.setUpdatedAt(CREATED_AT);
        entityManager.persist(method);
    }
    private void payment(UUID id, String number, UUID customerId,
            UUID invoiceId, String methodKey, String amountValue) {
        if (entityManager.find(PaymentEntity.class, id) == null) {
            BigDecimal amount = new BigDecimal(amountValue);
            var payment = new PaymentEntity();
            payment.setId(id);
            payment.setPaymentNumber(number);
            payment.setCustomerId(customerId);
            payment.setAmount(amount);
            payment.setPaymentMethodId(id("payment-method." + methodKey));
            payment.setSource("PAYMENT_METHOD");
            payment.setReferenceNumber("SEED-" + number);
            payment.setPaymentDate(LocalDate.of(2025, 1, 10));
            payment.setNotes("Deterministic seed payment");
            payment.setStatus("VERIFIED");
            payment.setAppliedAmount(amount);
            payment.setCreatedBy(ADMIN_USER);
            payment.setCreatedByName("Cashly Administrator");
            payment.setCreatedAt(CREATED_AT);
            payment.setUpdatedAt(CREATED_AT);
            payment.setVerifiedBy(ADMIN_USER);
            payment.setVerifiedByName("Cashly Administrator");
            payment.setVerifiedAt(CREATED_AT);
            entityManager.persist(payment);
        }
        UUID allocationId = id("payment-allocation." + number);
        if (entityManager.find(PaymentAllocationEntity.class,
                allocationId) == null) {
            var allocation = new PaymentAllocationEntity();
            allocation.setId(allocationId);
            allocation.setPaymentId(id);
            allocation.setInvoiceId(invoiceId);
            allocation.setAllocatedAmount(new BigDecimal(amountValue));
            allocation.setCreatedAt(CREATED_AT);
            entityManager.persist(allocation);
        }
    }
}
