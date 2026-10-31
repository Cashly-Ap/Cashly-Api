package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.finance.billing.credit_notes.infrastructure.persistence.entities.CreditNoteEntity;
import com.cashly.cashly_api.finance.billing.credit_notes.infrastructure.persistence.entities.CreditNoteNumberSequenceEntity;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V18CreditNotesSeed implements SeedStep {
    private final EntityManager entityManager;
    public V18CreditNotesSeed(EntityManager entityManager) { this.entityManager = entityManager; }
    @Override public int version() { return 18; }
    @Override public void seed() {
        note("one", "CN-2025-0001", CUSTOMER_ONE, INVOICE_ONE, "1.50");
        note("two", "CN-2025-0002", CUSTOMER_TWO, INVOICE_TWO, "0.50");
        if (entityManager.find(CreditNoteNumberSequenceEntity.class, 2025) == null) { var s = new CreditNoteNumberSequenceEntity(); s.setYear(2025); s.setNextValue(3); entityManager.persist(s); }
    }
    private void note(String key, String number, UUID customerId, UUID invoiceId, String amount) {
        UUID id = id("credit-note." + key); if (entityManager.find(CreditNoteEntity.class, id) != null) return;
        var note = new CreditNoteEntity(); note.setId(id); note.setNoteNumber(number); note.setCustomerId(customerId); note.setInvoiceId(invoiceId);
        note.setAmount(new BigDecimal(amount)); note.setReason("Seed commercial adjustment"); note.setCreatedBy(ADMIN_USER); note.setCreatedByName("Cashly Administrator");
        note.setCreatedAt(LocalDateTime.of(2025, 1, 12, 8, 0)); note.setUpdatedAt(LocalDateTime.of(2025, 1, 12, 8, 0)); entityManager.persist(note);
    }
}
