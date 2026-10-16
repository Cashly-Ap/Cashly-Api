package com.cashly.cashly_api.customers.profile.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

public class AddressId {
    private final UUID value;

    public AddressId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Address ID cannot be null");
        }
        this.value = value;
    }

    public static AddressId generate() {
        return new AddressId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AddressId that = (AddressId) obj;
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
