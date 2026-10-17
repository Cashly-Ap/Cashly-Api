package com.cashly.cashly_api.partners.suppliers.domain.valueobjects;

import java.util.Objects;

public class SupplierAlias {
    private final String value;

    public SupplierAlias(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Supplier alias cannot be null or blank");
        }
        if (value.trim().length() > 100) {
            throw new IllegalArgumentException("Supplier alias must not exceed 100 characters");
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
        SupplierAlias that = (SupplierAlias) obj;
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
