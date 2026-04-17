package com.parsun.parsun_api.customers.domain.valueobjects;

import java.util.Objects;

public class LegalName {
    private final String value;

    public LegalName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Legal name cannot be null or blank");
        }
        if (value.trim().length() < 2 || value.trim().length() > 200) {
            throw new IllegalArgumentException("Legal name must be between 2 and 200 characters");
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
        LegalName that = (LegalName) obj;
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
