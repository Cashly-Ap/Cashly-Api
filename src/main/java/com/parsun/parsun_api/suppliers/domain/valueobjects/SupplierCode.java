package com.parsun.parsun_api.suppliers.domain.valueobjects;

import java.util.Objects;

public class SupplierCode {
    private final Long value;

    public SupplierCode(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Supplier code must be a positive number");
        }
        this.value = value;
    }

    public static SupplierCode from(long value) {
        return new SupplierCode(value);
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SupplierCode that = (SupplierCode) obj;
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
