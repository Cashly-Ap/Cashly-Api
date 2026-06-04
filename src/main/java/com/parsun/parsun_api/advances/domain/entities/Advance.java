package com.parsun.parsun_api.advances.domain.entities;

import com.parsun.parsun_api.advances.domain.valueobjects.AdvanceId;
import com.parsun.parsun_api.advances.domain.valueobjects.AdvanceNumber;
import com.parsun.parsun_api.advances.domain.valueobjects.AdvanceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Advance {

    private final AdvanceId id;
    private final AdvanceNumber advanceNumber;
    private final UUID customerId;
    private final BigDecimal amount;
    private BigDecimal remainingAmount;
    private AdvanceStatus status;
    private String notes;
    private final UUID createdBy;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Advance(AdvanceId id, AdvanceNumber advanceNumber, UUID customerId,
                    BigDecimal amount, BigDecimal remainingAmount, AdvanceStatus status,
                    String notes, UUID createdBy,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.advanceNumber = advanceNumber;
        this.customerId = customerId;
        this.amount = amount;
        this.remainingAmount = remainingAmount;
        this.status = status;
        this.notes = notes;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Advance create(AdvanceNumber advanceNumber, UUID customerId,
                                  BigDecimal amount, String notes, UUID createdBy) {
        Objects.requireNonNull(advanceNumber, "Advance number cannot be null");
        Objects.requireNonNull(customerId, "Customer id cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        Objects.requireNonNull(createdBy, "Created by cannot be null");

        LocalDateTime now = LocalDateTime.now();
        return new Advance(
                AdvanceId.generate(),
                advanceNumber,
                customerId,
                amount,
                amount,
                AdvanceStatus.ACTIVE,
                notes,
                createdBy,
                now,
                now
        );
    }

    public static Advance reconstitute(AdvanceId id, AdvanceNumber advanceNumber, UUID customerId,
                                        BigDecimal amount, BigDecimal remainingAmount,
                                        AdvanceStatus status, String notes,
                                        UUID createdBy, LocalDateTime createdAt,
                                        LocalDateTime updatedAt) {
        return new Advance(id, advanceNumber, customerId, amount, remainingAmount,
                status, notes, createdBy, createdAt, updatedAt);
    }

    public void applyConsumption(BigDecimal consumptionAmount) {
        if (status == AdvanceStatus.CANCELLED) {
            throw new IllegalStateException("Cannot consume a cancelled advance");
        }
        if (status == AdvanceStatus.FULLY_CONSUMED) {
            throw new IllegalStateException("Advance is already fully consumed");
        }
        Objects.requireNonNull(consumptionAmount, "Consumption amount cannot be null");
        if (consumptionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Consumption amount must be greater than zero");
        }
        if (consumptionAmount.compareTo(remainingAmount) > 0) {
            throw new IllegalArgumentException("Consumption amount exceeds remaining advance balance");
        }

        this.remainingAmount = this.remainingAmount.subtract(consumptionAmount);

        if (this.remainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.status = AdvanceStatus.FULLY_CONSUMED;
        } else {
            this.status = AdvanceStatus.PARTIALLY_CONSUMED;
        }

        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (status == AdvanceStatus.CANCELLED) {
            throw new IllegalStateException("Advance is already cancelled");
        }
        if (status == AdvanceStatus.FULLY_CONSUMED) {
            throw new IllegalStateException("Cannot cancel a fully consumed advance");
        }
        this.status = AdvanceStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public AdvanceId getId() { return id; }
    public AdvanceNumber getAdvanceNumber() { return advanceNumber; }
    public UUID getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public AdvanceStatus getStatus() { return status; }
    public String getNotes() { return notes; }
    public UUID getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Advance advance = (Advance) o;
        return Objects.equals(id, advance.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Advance{id=" + id + ", advanceNumber=" + advanceNumber + ", amount=" + amount + ", remainingAmount=" + remainingAmount + ", status=" + status + "}";
    }
}
