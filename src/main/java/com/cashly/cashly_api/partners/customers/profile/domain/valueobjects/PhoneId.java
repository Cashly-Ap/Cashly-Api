package com.cashly.cashly_api.partners.customers.profile.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

public class PhoneId {
    private final UUID value;

    public PhoneId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Phone ID cannot be null");
        }
        this.value = value;
    }

    public static PhoneId generate() {
        return new PhoneId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PhoneId that = (PhoneId) obj;
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
