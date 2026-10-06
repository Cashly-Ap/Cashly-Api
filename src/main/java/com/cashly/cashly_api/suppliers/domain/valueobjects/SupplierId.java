package com.cashly.cashly_api.suppliers.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

public class SupplierId {
    private final UUID value;

    public SupplierId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Supplier ID cannot be null");
        }
        this.value = value;
    }

    public static SupplierId generate() {
        return new SupplierId(UUID.randomUUID());
    }

    public static SupplierId from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier ID string cannot be null or empty");
        }
        try {
            return new SupplierId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Supplier ID format: " + value, e);
        }
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SupplierId that = (SupplierId) obj;
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
