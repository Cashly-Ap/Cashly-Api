package com.parsun.parsun_api.finances.domain.entities;

import com.parsun.parsun_api.finances.domain.valueobjects.DebitNoteId;
import com.parsun.parsun_api.finances.domain.valueobjects.DebitNoteNumber;
import com.parsun.parsun_api.finances.domain.valueobjects.DebitNoteStatus;

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
    private DebitNoteStatus status;
    private BigDecimal appliedAmount;
    private final UUID createdBy;
    private final String createdByName;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private DebitNote(DebitNoteId id, DebitNoteNumber noteNumber, UUID customerId, UUID invoiceId,
                      BigDecimal amount, String reason, DebitNoteStatus status, BigDecimal appliedAmount,
                      UUID createdBy, String createdByName, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.noteNumber = noteNumber;
        this.customerId = customerId;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.reason = reason;
        this.status = status;
        this.appliedAmount = appliedAmount;
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
                DebitNoteStatus.DRAFT, BigDecimal.ZERO, createdBy, createdByName, now, now);
    }

    public static DebitNote reconstitute(DebitNoteId id, DebitNoteNumber noteNumber, UUID customerId,
                                         UUID invoiceId, BigDecimal amount, String reason,
                                         DebitNoteStatus status, BigDecimal appliedAmount,
                                         UUID createdBy, String createdByName,
                                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new DebitNote(id, noteNumber, customerId, invoiceId, amount, reason, status, appliedAmount,
                createdBy, createdByName, createdAt, updatedAt);
    }

    public void apply(BigDecimal amount) {
        if (this.status != DebitNoteStatus.DRAFT && this.status != DebitNoteStatus.APPLIED) {
            throw new IllegalStateException("Only DRAFT or APPLIED debit notes can be applied");
        }
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        BigDecimal newApplied = this.appliedAmount.add(amount);
        if (newApplied.compareTo(this.amount) > 0) {
            throw new IllegalArgumentException("Applied amount cannot exceed debit note total");
        }
        this.appliedAmount = newApplied;
        if (this.appliedAmount.compareTo(this.amount) == 0) {
            this.status = DebitNoteStatus.APPLIED;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status == DebitNoteStatus.CANCELLED) {
            throw new IllegalStateException("Debit note is already cancelled");
        }
        this.status = DebitNoteStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public DebitNoteId getId() { return id; }
    public DebitNoteNumber getNoteNumber() { return noteNumber; }
    public UUID getCustomerId() { return customerId; }
    public UUID getInvoiceId() { return invoiceId; }
    public BigDecimal getAmount() { return amount; }
    public String getReason() { return reason; }
    public DebitNoteStatus getStatus() { return status; }
    public BigDecimal getAppliedAmount() { return appliedAmount; }
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
        return "DebitNote{id=" + id + ", noteNumber=" + noteNumber + ", amount=" + amount + ", status=" + status + "}";
    }
}
