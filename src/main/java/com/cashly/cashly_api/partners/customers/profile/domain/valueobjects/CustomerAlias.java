package com.cashly.cashly_api.partners.customers.profile.domain.valueobjects;

import java.util.Objects;

public class CustomerAlias {
    private final String value;

    public CustomerAlias(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Customer alias cannot be null or blank");
        }
        if (value.trim().length() < 2 || value.trim().length() > 100) {
            throw new IllegalArgumentException("Customer alias must be between 2 and 100 characters");
        }
        this.value = value.trim();
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CustomerAlias that = (CustomerAlias) obj;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
