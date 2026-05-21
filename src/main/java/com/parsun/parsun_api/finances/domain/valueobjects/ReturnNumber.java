package com.parsun.parsun_api.finances.domain.valueobjects;

import java.util.Objects;

public record ReturnNumber(String value) {

    public ReturnNumber {
        Objects.requireNonNull(value, "ReturnNumber cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("ReturnNumber cannot be blank");
        }
    }

    public static ReturnNumber of(int year, int sequence) {
        return new ReturnNumber("RET-%d-%04d".formatted(year, sequence));
    }
}
