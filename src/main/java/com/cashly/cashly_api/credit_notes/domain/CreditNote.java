package com.cashly.cashly_api.credit_notes.domain;

import com.cashly.cashly_api.credit_notes.domain.valueobjects.CreditNoteId;
import com.cashly.cashly_api.credit_notes.domain.valueobjects.CreditNoteNumber;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class CreditNote {

    private final CreditNoteId id;
    private final CreditNoteNumber noteNumber;
    private final UUID customerId;
    private final UUID invoiceId;
    private final BigDecimal amount;
    private final String reason;
    private final UUID createdBy;
    private final String createdByName;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CreditNote(CreditNoteId id, CreditNoteNumber noteNumber, UUID customerId, UUID invoiceId,
                       BigDecimal amount, String reason,
                       UUID createdBy, String createdByName, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.noteNumber = noteNumber;
        this.customerId = customerId;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.reason = reason;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CreditNote create(CreditNoteNumber noteNumber, UUID customerId, UUID invoiceId,
                                    BigDecimal amount, String reason, UUID createdBy, String createdByName) {
        Objects.requireNonNull(noteNumber, "Credit note number cannot be null");
        Objects.requireNonNull(customerId, "Customer id cannot be null");
        Objects.requireNonNull(invoiceId, "Invoice id cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        Objects.requireNonNull(reason, "Reason cannot be null");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("Reason cannot be blank");
        }
        Objects.requireNonNull(createdBy, "Created by cannot be null");
        Objects.requireNonNull(createdByName, "Created by name cannot be null");

        LocalDateTime now = LocalDateTime.now();
        return new CreditNote(CreditNoteId.generate(), noteNumber, customerId, invoiceId, amount, reason,
                createdBy, createdByName, now, now);
    }

    public static CreditNote reconstitute(CreditNoteId id, CreditNoteNumber noteNumber, UUID customerId,
                                          UUID invoiceId, BigDecimal amount, String reason,
                                          UUID createdBy, String createdByName,
                                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new CreditNote(id, noteNumber, customerId, invoiceId, amount, reason,
                createdBy, createdByName, createdAt, updatedAt);
    }

    public CreditNoteId getId() { return id; }
    public CreditNoteNumber getNoteNumber() { return noteNumber; }
    public UUID getCustomerId() { return customerId; }
    public UUID getInvoiceId() { return invoiceId; }
    public BigDecimal getAmount() { return amount; }
    public String getReason() { return reason; }
    public UUID getCreatedBy() { return createdBy; }
    public String getCreatedByName() { return createdByName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreditNote that = (CreditNote) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "CreditNote{id=" + id + ", noteNumber=" + noteNumber + ", amount=" + amount + "}";
    }
}
