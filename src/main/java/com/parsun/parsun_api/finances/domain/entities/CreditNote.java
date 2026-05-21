package com.parsun.parsun_api.finances.domain.entities;

import com.parsun.parsun_api.finances.domain.valueobjects.CreditNoteId;
import com.parsun.parsun_api.finances.domain.valueobjects.CreditNoteNumber;
import com.parsun.parsun_api.finances.domain.valueobjects.CreditNoteStatus;

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
    private CreditNoteStatus status;
    private BigDecimal appliedAmount;
    private final UUID createdBy;
    private final String createdByName;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CreditNote(CreditNoteId id, CreditNoteNumber noteNumber, UUID customerId, UUID invoiceId,
                       BigDecimal amount, String reason, CreditNoteStatus status, BigDecimal appliedAmount,
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
                CreditNoteStatus.DRAFT, BigDecimal.ZERO, createdBy, createdByName, now, now);
    }

    public static CreditNote reconstitute(CreditNoteId id, CreditNoteNumber noteNumber, UUID customerId,
                                          UUID invoiceId, BigDecimal amount, String reason,
                                          CreditNoteStatus status, BigDecimal appliedAmount,
                                          UUID createdBy, String createdByName,
                                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new CreditNote(id, noteNumber, customerId, invoiceId, amount, reason, status, appliedAmount,
                createdBy, createdByName, createdAt, updatedAt);
    }

    public void apply(BigDecimal amount) {
        if (this.status != CreditNoteStatus.DRAFT && this.status != CreditNoteStatus.APPLIED) {
            throw new IllegalStateException("Only DRAFT or APPLIED credit notes can be applied");
        }
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        BigDecimal newApplied = this.appliedAmount.add(amount);
        if (newApplied.compareTo(this.amount) > 0) {
            throw new IllegalArgumentException("Applied amount cannot exceed credit note total");
        }
        this.appliedAmount = newApplied;
        if (this.appliedAmount.compareTo(this.amount) == 0) {
            this.status = CreditNoteStatus.APPLIED;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status == CreditNoteStatus.CANCELLED) {
            throw new IllegalStateException("Credit note is already cancelled");
        }
        this.status = CreditNoteStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public CreditNoteId getId() { return id; }
    public CreditNoteNumber getNoteNumber() { return noteNumber; }
    public UUID getCustomerId() { return customerId; }
    public UUID getInvoiceId() { return invoiceId; }
    public BigDecimal getAmount() { return amount; }
    public String getReason() { return reason; }
    public CreditNoteStatus getStatus() { return status; }
    public BigDecimal getAppliedAmount() { return appliedAmount; }
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
        return "CreditNote{id=" + id + ", noteNumber=" + noteNumber + ", amount=" + amount + ", status=" + status + "}";
    }
}
