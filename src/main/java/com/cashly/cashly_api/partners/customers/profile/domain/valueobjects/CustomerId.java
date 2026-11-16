package com.cashly.cashly_api.partners.customers.profile.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

public class CustomerId {
    private final UUID value;

    public CustomerId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        this.value = value;
    }

    public static CustomerId generate() {
        return new CustomerId(UUID.randomUUID());
    }

    public static CustomerId from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Customer ID string cannot be null or empty");
        }
        try {
            return new CustomerId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid Customer ID format: " + value, e);
        }
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        CustomerId that = (CustomerId) obj;
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
