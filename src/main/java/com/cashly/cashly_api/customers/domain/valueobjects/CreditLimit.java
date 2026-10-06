package com.cashly.cashly_api.customers.domain.valueobjects;

import java.math.BigDecimal;
import java.util.Objects;

public class CreditLimit {
    private final BigDecimal value;

    public CreditLimit(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Credit limit cannot be null");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Credit limit must be non-negative");
        }
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CreditLimit that = (CreditLimit) obj;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
