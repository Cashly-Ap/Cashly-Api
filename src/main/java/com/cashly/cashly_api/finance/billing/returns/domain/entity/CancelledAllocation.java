package com.cashly.cashly_api.finance.billing.returns.domain.entity;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class CancelledAllocation {

    private final UUID paymentId;
    private final UUID invoiceId;
    private final BigDecimal amount;

    private CancelledAllocation(UUID paymentId, UUID invoiceId,
            BigDecimal amount) {
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.amount = amount;
    }

    public static CancelledAllocation create(UUID paymentId, UUID invoiceId,
            BigDecimal amount) {
        Objects.requireNonNull(paymentId, "Payment id cannot be null");
        Objects.requireNonNull(invoiceId, "Invoice id cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero");
        }
        return new CancelledAllocation(paymentId, invoiceId, amount);
    }

    public static CancelledAllocation reconstitute(UUID paymentId,
            UUID invoiceId, BigDecimal amount) {
        return new CancelledAllocation(paymentId, invoiceId, amount);
    }

    public UUID getPaymentId() {
        return paymentId;
    }
    public UUID getInvoiceId() {
        return invoiceId;
    }
    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CancelledAllocation that = (CancelledAllocation) o;
        return Objects.equals(paymentId, that.paymentId)
                && Objects.equals(invoiceId, that.invoiceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId, invoiceId);
    }

    @Override
    public String toString() {
        return "CancelledAllocation{paymentId=" + paymentId + ", invoiceId="
                + invoiceId + ", amount=" + amount + "}";
    }
}
