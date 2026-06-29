package com.parsun.parsun_api.suppliers.domain.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

public class Rif {
    private static final Pattern RIF_PATTERN = Pattern.compile("^[VJGECP]-\\d{8}-\\d$");

    private final String value;

    public Rif(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("RIF cannot be null or blank");
        }
        String trimmed = value.trim().toUpperCase();
        if (!RIF_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid RIF format. Expected format: X-XXXXXXXX-X (e.g., J-12345678-9)");
        }
        this.value = trimmed;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Rif that = (Rif) obj;
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
