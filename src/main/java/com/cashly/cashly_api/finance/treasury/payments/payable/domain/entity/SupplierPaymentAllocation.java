package com.cashly.cashly_api.finance.treasury.payments.payable.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class SupplierPaymentAllocation {

    private final UUID id;
    private final UUID paymentId;
    private final UUID purchaseId;
    private final BigDecimal allocatedAmount;
    private final LocalDateTime createdAt;

    private SupplierPaymentAllocation(UUID id, UUID paymentId,
            UUID purchaseId, BigDecimal allocatedAmount,
            LocalDateTime createdAt) {
        this.id = id;
        this.paymentId = paymentId;
        this.purchaseId = purchaseId;
        this.allocatedAmount = allocatedAmount;
        this.createdAt = createdAt;
    }

    public static SupplierPaymentAllocation create(UUID paymentId,
            UUID purchaseId, BigDecimal allocatedAmount) {
        Objects.requireNonNull(paymentId, "Payment id cannot be null");
        Objects.requireNonNull(purchaseId, "Purchase id cannot be null");
        Objects.requireNonNull(allocatedAmount,
                "Allocated amount cannot be null");
        if (allocatedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Allocated amount must be greater than zero");
        }
        return new SupplierPaymentAllocation(UUID.randomUUID(), paymentId,
                purchaseId, allocatedAmount, LocalDateTime.now());
    }

    public static SupplierPaymentAllocation reconstitute(UUID id,
            UUID paymentId, UUID purchaseId, BigDecimal allocatedAmount,
            LocalDateTime createdAt) {
        return new SupplierPaymentAllocation(id, paymentId, purchaseId,
                allocatedAmount, createdAt);
    }

    public UUID getId() {
        return id;
    }
    public UUID getPaymentId() {
        return paymentId;
    }
    public UUID getPurchaseId() {
        return purchaseId;
    }
    public BigDecimal getAllocatedAmount() {
        return allocatedAmount;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
