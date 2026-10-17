package com.cashly.cashly_api.partners.suppliers.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

public class EmailId {
    private final UUID value;

    public EmailId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Email ID cannot be null");
        }
        this.value = value;
    }

    public static EmailId generate() {
        return new EmailId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EmailId that = (EmailId) obj;
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
