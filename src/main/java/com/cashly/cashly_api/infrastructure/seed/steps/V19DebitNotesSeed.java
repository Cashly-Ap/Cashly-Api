package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.finance.billing.debit_notes.infrastructure.persistence.entities.DebitNoteEntity;
import com.cashly.cashly_api.finance.billing.debit_notes.infrastructure.persistence.entities.DebitNoteNumberSequenceEntity;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V19DebitNotesSeed implements SeedStep {
    private final EntityManager entityManager;
    public V19DebitNotesSeed(EntityManager entityManager) { this.entityManager = entityManager; }
    @Override public int version() { return 19; }
    @Override public void seed() {
        note("one", "DN-2025-0001", CUSTOMER_ONE, INVOICE_ONE, "0.75");
        note("two", "DN-2025-0002", CUSTOMER_TWO, INVOICE_TWO, "0.25");
        if (entityManager.find(DebitNoteNumberSequenceEntity.class, 2025) == null) { var s = new DebitNoteNumberSequenceEntity(); s.setYear(2025); s.setNextValue(3); entityManager.persist(s); }
    }
    private void note(String key, String number, UUID customerId, UUID invoiceId, String amount) {
        UUID id = id("debit-note." + key); if (entityManager.find(DebitNoteEntity.class, id) != null) return;
        var note = new DebitNoteEntity(); note.setId(id); note.setNoteNumber(number); note.setCustomerId(customerId); note.setInvoiceId(invoiceId);
        note.setAmount(new BigDecimal(amount)); note.setReason("Seed service adjustment"); note.setCreatedBy(ADMIN_USER); note.setCreatedByName("Cashly Administrator");
        note.setCreatedAt(LocalDateTime.of(2025, 1, 12, 9, 0)); note.setUpdatedAt(LocalDateTime.of(2025, 1, 12, 9, 0)); entityManager.persist(note);
    }
}
