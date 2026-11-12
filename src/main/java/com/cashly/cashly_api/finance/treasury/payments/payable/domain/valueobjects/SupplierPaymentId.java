package com.cashly.cashly_api.finance.treasury.payments.payable.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record SupplierPaymentId(UUID value) {

    public SupplierPaymentId {
        Objects.requireNonNull(value, "SupplierPaymentId cannot be null");
    }

    public static SupplierPaymentId of(UUID value) {
        return new SupplierPaymentId(value);
    }

    public static SupplierPaymentId generate() {
        return new SupplierPaymentId(UUID.randomUUID());
    }
}
