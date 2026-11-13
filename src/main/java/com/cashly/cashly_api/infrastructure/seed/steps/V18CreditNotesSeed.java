package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.finance.billing.credit_notes.infrastructure.persistence.entities.CreditNoteEntity;
import com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.cashly.cashly_api.finance.billing.credit_notes.infrastructure.persistence.entities.CreditNoteNumberSequenceEntity;
import com.cashly.cashly_api.infrastructure.seed.SeedDocuments;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V18CreditNotesSeed implements SeedStep {
    private final EntityManager entityManager;
    public V18CreditNotesSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    public int version() {
        return 18;
    }
    @Override
    public void seed() {
        note("one", "CN-2026-0001", CUSTOMER_ONE, INVOICE_ONE,
                new BigDecimal("1.50"), LocalDateTime.of(2026, 1, 12, 8, 0));
        note("two", "CN-2026-0002", CUSTOMER_TWO, INVOICE_TWO,
                new BigDecimal("0.50"), LocalDateTime.of(2026, 1, 12, 8, 0));
        for (var sample : SeedDocuments.additional()) {
            if (sample.number() % 3 != 0) {
                continue;
            }
            var invoice = entityManager.find(InvoiceEntity.class,
                    id("invoice." + sample.key()));
            if (invoice == null || "DRAFT".equals(invoice.getStatus())
                    || "CANCELLED".equals(invoice.getStatus())) {
                continue;
            }
            BigDecimal amount = invoice.getSubtotal()
                    .multiply(new BigDecimal("0.10"))
                    .setScale(2, RoundingMode.HALF_UP);
            note(sample.key(), sample.documentNumber("CN"),
                    sample.customerId(), id("invoice." + sample.key()),
                    amount, sample.createdAt(12, 0));
        }
        for (int year : SeedDocuments.years()) {
            var sequence = entityManager
                    .find(CreditNoteNumberSequenceEntity.class, year);
            int nextValue = SeedDocuments.nextNumberFor(year);
            if (sequence == null) {
                sequence = new CreditNoteNumberSequenceEntity();
                sequence.setYear(year);
                sequence.setNextValue(nextValue);
                entityManager.persist(sequence);
            } else if (sequence.getNextValue() < nextValue) {
                sequence.setNextValue(nextValue);
            }
        }
    }

    private void note(String key, String number, UUID customerId,
            UUID invoiceId, BigDecimal amount, LocalDateTime createdAt) {
        UUID id = id("credit-note." + key);
        if (entityManager.find(CreditNoteEntity.class, id) != null)
            return;
        var note = new CreditNoteEntity();
        note.setId(id);
        note.setNoteNumber(number);
        note.setCustomerId(customerId);
        note.setInvoiceId(invoiceId);
        note.setAmount(amount);
        note.setReason("Seed commercial adjustment");
        note.setCreatedBy(ADMIN_USER);
        note.setCreatedByName("Cashly Administrator");
        note.setCreatedAt(createdAt);
        note.setUpdatedAt(createdAt);
        entityManager.persist(note);
    }
}
