package com.cashly.cashly_api.partners.customers.profile.domain.valueobjects;

import java.util.Objects;

public class CreditDays {
    private final int value;

    public CreditDays(int value) {
        if (value < 0 || value > 365) {
            throw new IllegalArgumentException("Credit days must be between 0 and 365");
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CreditDays that = (CreditDays) obj;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
