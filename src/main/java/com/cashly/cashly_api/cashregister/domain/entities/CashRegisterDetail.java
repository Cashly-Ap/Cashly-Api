package com.cashly.cashly_api.cashregister.domain.entities;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class CashRegisterDetail {

    private final UUID id;
    private final UUID paymentMethodId;
    private final String paymentMethodName;
    private final int transactionCount;
    private final BigDecimal expectedAmount;

    private CashRegisterDetail(UUID id, UUID paymentMethodId, String paymentMethodName,
                              int transactionCount, BigDecimal expectedAmount) {
        this.id = Objects.requireNonNull(id, "Detail id must not be null");
        this.paymentMethodName = Objects.requireNonNull(paymentMethodName, "Payment method name must not be null");
        this.paymentMethodId = paymentMethodId;
        this.transactionCount = transactionCount;
        this.expectedAmount = Objects.requireNonNull(expectedAmount, "Expected amount must not be null");
    }

    public static CashRegisterDetail create(UUID paymentMethodId, String paymentMethodName,
                                           int transactionCount, BigDecimal expectedAmount) {
        if (expectedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Expected amount must not be negative");
        }
        return new CashRegisterDetail(
                UUID.randomUUID(),
                paymentMethodId,
                paymentMethodName,
                transactionCount,
                expectedAmount
        );
    }

    public static CashRegisterDetail reconstitute(UUID id, UUID paymentMethodId, String paymentMethodName,
                                                  int transactionCount, BigDecimal expectedAmount) {
        return new CashRegisterDetail(id, paymentMethodId, paymentMethodName,
                transactionCount, expectedAmount);
    }

    public UUID getId() {
        return id;
    }

    public UUID getPaymentMethodId() {
        return paymentMethodId;
    }

    public String getPaymentMethodName() {
        return paymentMethodName;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public BigDecimal getExpectedAmount() {
        return expectedAmount;
    }
}
