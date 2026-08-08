package com.parsun.parsun_api.auth.user.domain.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

public class Password {
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 100;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

    private final String hashedValue;

    private Password(String hashedValue) {
        if (hashedValue == null || hashedValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Hashed password cannot be null or empty");
        }
        this.hashedValue = hashedValue;
    }

    public static Password fromHash(String hashedValue) {
        return new Password(hashedValue);
    }

    public static void validateRawPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("La contraseña es requerida");
        }

        if (rawPassword.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("La contraseña debe tener al menos " + MIN_LENGTH + " caracteres");
        }

        if (rawPassword.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("La contraseña no puede exceder " + MAX_LENGTH + " caracteres");
        }

        if (!UPPERCASE_PATTERN.matcher(rawPassword).matches()) {
            throw new IllegalArgumentException("La contraseña debe contener: letra mayúscula");
        }

        if (!LOWERCASE_PATTERN.matcher(rawPassword).matches()) {
            throw new IllegalArgumentException("La contraseña debe contener: letra minúscula");
        }

        if (!DIGIT_PATTERN.matcher(rawPassword).matches()) {
            throw new IllegalArgumentException("La contraseña debe contener: número");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(rawPassword).matches()) {
            throw new IllegalArgumentException("La contraseña debe contener: carácter especial");
        }
    }

    public String getHashedValue() {
        return hashedValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Password password = (Password) obj;
        return Objects.equals(hashedValue, password.hashedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(hashedValue);
    }

    @Override
    public String toString() {
        return "Password{hashedValue='***'}";
    }
}
