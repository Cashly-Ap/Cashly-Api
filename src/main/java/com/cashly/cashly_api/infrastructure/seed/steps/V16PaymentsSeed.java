package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;
import static com.cashly.cashly_api.infrastructure.seed.SeedDocuments.*;

import com.cashly.cashly_api.finance.treasury.payment_methods.infrastructure.persistence.entities.PaymentMethodEntity;
import com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.persistence.entities.PaymentAllocationEntity;
import com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.persistence.entities.PaymentEntity;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentStatus;
import com.cashly.cashly_api.infrastructure.seed.SeedDocuments;
import com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.PaymentType;
import java.math.RoundingMode;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V16PaymentsSeed implements SeedStep {
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1,
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
        method("card", "CARD", "Credit/Debit card");
        method("mobile", "MOBILE_PAYMENT", "Mobile payment");
        method("check", "CHECK", "Check");
        payment(PAYMENT_ONE, "PAY-2026-0001", CUSTOMER_ONE, INVOICE_ONE, "cash",
                "10.00");
        payment(PAYMENT_TWO, "PAY-2026-0002", CUSTOMER_TWO, INVOICE_TWO,
                "transfer", "3.00");
        for (var sample : SeedDocuments.additional()) {
            var invoice = entityManager.find(InvoiceEntity.class,
                    id("invoice." + sample.key()));
            var scenario = sample.scenario();
            if (invoice == null
                    || !PENDING_LINKS_NOTES.equals(invoice.getNotes())
                    || !scenario.invoice().name().equals(invoice.getStatus())) {
                continue;
            }
            if (scenario.invoice() == InvoiceStatus.PAID
                    || scenario.invoice() == InvoiceStatus.PARTIALLY_PAID) {
                var amount = scenario.invoice() == InvoiceStatus.PAID
                        ? invoice.getSubtotal()
                        : invoice.getSubtotal().divide(PARTIAL_PAYMENT_DIVISOR,
                                MONEY_SCALE, RoundingMode.HALF_UP);
                payment(sample, invoice, scenario, amount);
            }
        }
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
        var existing = entityManager.find(PaymentEntity.class, id);
        if (existing != null && "VERIFIED".equals(existing.getStatus())) {
            existing.setStatus(PaymentStatus.APPLIED.name());
        }
        if (existing == null) {
            BigDecimal amount = new BigDecimal(amountValue);
            var payment = new PaymentEntity();
            payment.setId(id);
            payment.setPaymentNumber(number);
            payment.setCustomerId(customerId);
            payment.setAmount(amount);
            payment.setPaymentMethodId(id("payment-method." + methodKey));
            payment.setSource("PAYMENT_METHOD");
            payment.setReferenceNumber("SEED-" + number);
            payment.setPaymentDate(LocalDate.of(2026, 1, 10));
            payment.setNotes("Deterministic seed payment");
            payment.setStatus(PaymentStatus.APPLIED.name());
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

    private void payment(SeedDocuments.Sample sample, InvoiceEntity invoice,
            Scenario scenario, BigDecimal amount) {
        var paymentId = id("scenario-payment." + sample.key());
        if (entityManager.find(PaymentEntity.class, paymentId) == null) {
            var payment = new PaymentEntity();
            payment.setId(paymentId);
            payment.setPaymentNumber(sample.documentNumber("SEEDPAY"));
            payment.setCustomerId(invoice.getCustomerId());
            payment.setAmount(amount);
            payment.setAppliedAmount(amount);
            payment.setPaymentMethodId(id("payment-method."
                    + (scenario.paymentType() == PaymentType.CASH
                            ? "cash"
                            : "transfer")));
            payment.setSource("PAYMENT_METHOD");
            payment.setReferenceNumber(sample.documentNumber("SEED"));
            payment.setPaymentDate(sample.date());
            payment.setStatus(PaymentStatus.APPLIED.name());
            payment.setNotes(SCENARIO_NOTES);
            payment.setCreatedBy(ADMIN_USER);
            payment.setCreatedByName("Cashly Administrator");
            payment.setVerifiedBy(ADMIN_USER);
            payment.setVerifiedByName("Cashly Administrator");
            payment.setVerifiedAt(sample.createdAt(11, 0));
            payment.setCreatedAt(sample.createdAt(11, 0));
            payment.setUpdatedAt(sample.createdAt(11, 0));
            entityManager.persist(payment);
        }
        var allocationId = id("scenario-payment-allocation." + sample.key());
        if (entityManager.find(PaymentAllocationEntity.class,
                allocationId) == null) {
            var allocation = new PaymentAllocationEntity();
            allocation.setId(allocationId);
            allocation.setPaymentId(paymentId);
            allocation.setInvoiceId(invoice.getId());
            allocation.setAllocatedAmount(amount);
            allocation.setCreatedAt(sample.createdAt(11, 0));
            entityManager.persist(allocation);
        }
    }

}
