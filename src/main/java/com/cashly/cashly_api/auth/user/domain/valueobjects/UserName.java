package com.cashly.cashly_api.auth.user.domain.valueobjects;

import java.util.Objects;

public class UserName {
    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 100;

    private final String value;

    public UserName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be null or empty");
        }

        String trimmedValue = value.trim();

        if (trimmedValue.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("User name must be at least " + MIN_LENGTH + " characters long");
        }

        if (trimmedValue.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("User name cannot exceed " + MAX_LENGTH + " characters");
        }

        this.value = trimmedValue;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserName userName = (UserName) obj;
        return Objects.equals(value, userName.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "UserName{" + "value='" + value + '\'' + '}';
    }
}
