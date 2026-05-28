package com.parsun.parsun_api.payments.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class PaymentAllocation {

    private final UUID id;
    private final UUID paymentId;
    private final UUID invoiceId;
    private final BigDecimal allocatedAmount;
    private final LocalDateTime createdAt;

    private PaymentAllocation(UUID id, UUID paymentId, UUID invoiceId,
                              BigDecimal allocatedAmount, LocalDateTime createdAt) {
        this.id = id;
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.allocatedAmount = allocatedAmount;
        this.createdAt = createdAt;
    }

    public static PaymentAllocation create(UUID paymentId, UUID invoiceId, BigDecimal allocatedAmount) {
        Objects.requireNonNull(paymentId, "Payment id cannot be null");
        Objects.requireNonNull(invoiceId, "Invoice id cannot be null");
        Objects.requireNonNull(allocatedAmount, "Allocated amount cannot be null");
        if (allocatedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Allocated amount must be greater than zero");
        }

        return new PaymentAllocation(UUID.randomUUID(), paymentId, invoiceId,
                allocatedAmount, LocalDateTime.now());
    }

    public static PaymentAllocation reconstitute(UUID id, UUID paymentId, UUID invoiceId,
                                                  BigDecimal allocatedAmount, LocalDateTime createdAt) {
        return new PaymentAllocation(id, paymentId, invoiceId, allocatedAmount, createdAt);
    }

    public UUID getId() { return id; }
    public UUID getPaymentId() { return paymentId; }
    public UUID getInvoiceId() { return invoiceId; }
    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
