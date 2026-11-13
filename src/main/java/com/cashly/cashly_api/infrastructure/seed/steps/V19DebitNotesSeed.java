package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.finance.billing.debit_notes.infrastructure.persistence.entities.DebitNoteEntity;
import com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.cashly.cashly_api.finance.billing.debit_notes.infrastructure.persistence.entities.DebitNoteNumberSequenceEntity;
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
public class V19DebitNotesSeed implements SeedStep {
    private final EntityManager entityManager;
    public V19DebitNotesSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    public int version() {
        return 19;
    }
    @Override
    public void seed() {
        note("one", "DN-2026-0001", CUSTOMER_ONE, INVOICE_ONE,
                new BigDecimal("0.75"), LocalDateTime.of(2026, 1, 12, 9, 0));
        note("two", "DN-2026-0002", CUSTOMER_TWO, INVOICE_TWO,
                new BigDecimal("0.25"), LocalDateTime.of(2026, 1, 12, 9, 0));
        for (var sample : SeedDocuments.additional()) {
            if (sample.number() % 4 != 0) {
                continue;
            }
            var invoice = entityManager.find(InvoiceEntity.class,
                    id("invoice." + sample.key()));
            if (invoice == null || "DRAFT".equals(invoice.getStatus())
                    || "CANCELLED".equals(invoice.getStatus())) {
                continue;
            }
            BigDecimal amount = invoice.getSubtotal()
                    .multiply(new BigDecimal("0.05"))
                    .setScale(2, RoundingMode.HALF_UP);
            note(sample.key(), sample.documentNumber("DN"),
                    sample.customerId(), id("invoice." + sample.key()),
                    amount, sample.createdAt(13, 0));
        }
        for (int year : SeedDocuments.years()) {
            var sequence = entityManager
                    .find(DebitNoteNumberSequenceEntity.class, year);
            int nextValue = SeedDocuments.nextNumberFor(year);
            if (sequence == null) {
                sequence = new DebitNoteNumberSequenceEntity();
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
        UUID id = id("debit-note." + key);
        if (entityManager.find(DebitNoteEntity.class, id) != null)
            return;
        var note = new DebitNoteEntity();
        note.setId(id);
        note.setNoteNumber(number);
        note.setCustomerId(customerId);
        note.setInvoiceId(invoiceId);
        note.setAmount(amount);
        note.setReason("Seed service adjustment");
        note.setCreatedBy(ADMIN_USER);
        note.setCreatedByName("Cashly Administrator");
        note.setCreatedAt(createdAt);
        note.setUpdatedAt(createdAt);
        entityManager.persist(note);
    }
}
