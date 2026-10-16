package com.cashly.cashly_api.debit_notes.domain;

import com.cashly.cashly_api.debit_notes.domain.valueobjects.DebitNoteId;
import com.cashly.cashly_api.debit_notes.domain.valueobjects.DebitNoteNumber;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class DebitNote {

    private final DebitNoteId id;
    private final DebitNoteNumber noteNumber;
    private final UUID customerId;
    private final UUID invoiceId;
    private final BigDecimal amount;
    private final String reason;
    private final UUID createdBy;
    private final String createdByName;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private DebitNote(DebitNoteId id, DebitNoteNumber noteNumber, UUID customerId, UUID invoiceId,
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

    public static DebitNote create(DebitNoteNumber noteNumber, UUID customerId, UUID invoiceId,
                                   BigDecimal amount, String reason, UUID createdBy, String createdByName) {
        Objects.requireNonNull(noteNumber, "Debit note number cannot be null");
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
        return new DebitNote(DebitNoteId.generate(), noteNumber, customerId, invoiceId, amount, reason,
                createdBy, createdByName, now, now);
    }

    public static DebitNote reconstitute(DebitNoteId id, DebitNoteNumber noteNumber, UUID customerId,
                                         UUID invoiceId, BigDecimal amount, String reason,
                                         UUID createdBy, String createdByName,
                                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new DebitNote(id, noteNumber, customerId, invoiceId, amount, reason,
                createdBy, createdByName, createdAt, updatedAt);
    }

    public DebitNoteId getId() { return id; }
    public DebitNoteNumber getNoteNumber() { return noteNumber; }
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
        DebitNote debitNote = (DebitNote) o;
        return Objects.equals(id, debitNote.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "DebitNote{id=" + id + ", noteNumber=" + noteNumber + ", amount=" + amount + "}";
    }
}
