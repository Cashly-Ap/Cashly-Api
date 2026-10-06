package com.cashly.cashly_api.auth.token.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

public class RefreshTokenId {
    private final UUID value;

    public RefreshTokenId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("RefreshToken ID cannot be null");
        }
        this.value = value;
    }

    public static RefreshTokenId generate() {
        return new RefreshTokenId(UUID.randomUUID());
    }

    public static RefreshTokenId from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("RefreshToken ID string cannot be null or empty");
        }
        try {
            return new RefreshTokenId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid RefreshToken ID format: " + value, e);
        }
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RefreshTokenId that = (RefreshTokenId) obj;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "RefreshTokenId{" + "value=" + value + '}';
    }
}
