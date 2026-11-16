package com.cashly.cashly_api.partners.customers.balance.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class CustomerBalance {

    private final UUID id;
    private final UUID customerId;
    private BigDecimal totalBalance;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CustomerBalance(UUID id, UUID customerId, BigDecimal totalBalance,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.totalBalance = totalBalance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CustomerBalance create(UUID customerId) {
        Objects.requireNonNull(customerId, "Customer id cannot be null");
        LocalDateTime now = LocalDateTime.now();
        return new CustomerBalance(UUID.randomUUID(), customerId,
                BigDecimal.ZERO, now, now);
    }

    public static CustomerBalance reconstitute(UUID id, UUID customerId,
            BigDecimal totalBalance, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new CustomerBalance(id, customerId, totalBalance, createdAt,
                updatedAt);
    }

    public void addToBalance(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero");
        }
        this.totalBalance = this.totalBalance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void consumeFromBalance(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero");
        }
        if (amount.compareTo(this.totalBalance) > 0) {
            throw new IllegalArgumentException(
                    "Amount exceeds current balance");
        }
        this.totalBalance = this.totalBalance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }
    public UUID getCustomerId() {
        return customerId;
    }
    public BigDecimal getTotalBalance() {
        return totalBalance;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CustomerBalance that = (CustomerBalance) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "CustomerBalance{id=" + id + ", customerId=" + customerId
                + ", totalBalance=" + totalBalance + "}";
    }
}
