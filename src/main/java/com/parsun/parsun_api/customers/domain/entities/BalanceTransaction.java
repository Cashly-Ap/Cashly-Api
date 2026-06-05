package com.parsun.parsun_api.customers.domain.entities;

import com.parsun.parsun_api.customers.domain.valueobjects.BalanceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class BalanceTransaction {

    private final UUID id;
    private final UUID customerId;
    private final BalanceType type;
    private final BigDecimal amount;
    private final BalanceType referenceType;
    private final UUID referenceId;
    private final String number;
    private final String referenceNumber;
    private final UUID createdBy;
    private final LocalDateTime createdAt;

    private BalanceTransaction(UUID id, UUID customerId, BalanceType type,
                               BigDecimal amount, BalanceType referenceType,
                               UUID referenceId, String number,
                               String referenceNumber, UUID createdBy,
                               LocalDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.type = type;
        this.amount = amount;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.number = number;
        this.referenceNumber = referenceNumber;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public static BalanceTransaction create(UUID customerId, BalanceType type,
                                             BigDecimal amount, BalanceType referenceType,
                                             UUID referenceId, String number,
                                             String referenceNumber, UUID createdBy) {
        Objects.requireNonNull(customerId, "Customer id cannot be null");
        Objects.requireNonNull(type, "Type cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(referenceType, "Reference type cannot be null");
        Objects.requireNonNull(createdBy, "Created by cannot be null");

        return new BalanceTransaction(
                UUID.randomUUID(),
                customerId,
                type,
                amount,
                referenceType,
                referenceId,
                number,
                referenceNumber,
                createdBy,
                LocalDateTime.now()
        );
    }

    public static BalanceTransaction reconstitute(UUID id, UUID customerId, BalanceType type,
                                                   BigDecimal amount, BalanceType referenceType,
                                                   UUID referenceId, String number,
                                                   String referenceNumber, UUID createdBy,
                                                   LocalDateTime createdAt) {
        return new BalanceTransaction(id, customerId, type, amount,
                referenceType, referenceId, number, referenceNumber, createdBy, createdAt);
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public BalanceType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public BalanceType getReferenceType() { return referenceType; }
    public UUID getReferenceId() { return referenceId; }
    public String getNumber() { return number; }
    public String getReferenceNumber() { return referenceNumber; }
    public UUID getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BalanceTransaction that = (BalanceTransaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "BalanceTransaction{id=" + id + ", customerId=" + customerId
                + ", type=" + type + ", amount=" + amount + "}";
    }
}
