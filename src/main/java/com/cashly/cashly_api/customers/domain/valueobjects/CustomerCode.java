package com.cashly.cashly_api.customers.domain.valueobjects;

import java.util.Objects;

public class CustomerCode {
    private final Long value;

    public CustomerCode(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Customer code must be a positive number");
        }
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CustomerCode that = (CustomerCode) obj;
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
