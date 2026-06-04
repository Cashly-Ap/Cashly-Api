package com.parsun.parsun_api.advances.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class AdvanceConsumption {

    private final UUID id;
    private final UUID advanceId;
    private final UUID invoiceId;
    private final BigDecimal amount;
    private final UUID paymentId;
    private final LocalDateTime createdAt;

    private AdvanceConsumption(UUID id, UUID advanceId, UUID invoiceId,
                               BigDecimal amount, UUID paymentId, LocalDateTime createdAt) {
        this.id = id;
        this.advanceId = advanceId;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.paymentId = paymentId;
        this.createdAt = createdAt;
    }

    public static AdvanceConsumption create(UUID advanceId, UUID invoiceId,
                                              BigDecimal amount, UUID paymentId) {
        Objects.requireNonNull(advanceId, "Advance id cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Consumption amount must be greater than zero");
        }

        return new AdvanceConsumption(UUID.randomUUID(), advanceId, invoiceId,
                amount, paymentId, LocalDateTime.now());
    }

    public static AdvanceConsumption reconstitute(UUID id, UUID advanceId, UUID invoiceId,
                                                   BigDecimal amount, UUID paymentId,
                                                   LocalDateTime createdAt) {
        return new AdvanceConsumption(id, advanceId, invoiceId, amount, paymentId, createdAt);
    }

    public UUID getId() { return id; }
    public UUID getAdvanceId() { return advanceId; }
    public UUID getInvoiceId() { return invoiceId; }
    public BigDecimal getAmount() { return amount; }
    public UUID getPaymentId() { return paymentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
