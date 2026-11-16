package com.cashly.cashly_api.platform.auth.authorization.domain.exceptions;

import java.util.List;

public class InvalidPermissionDependencyException extends RuntimeException {

    private final List<String> missing;

    public InvalidPermissionDependencyException(String message,
            List<String> missing) {
        super(message);
        this.missing = List.copyOf(missing);
    }

    public List<String> missing() {
        return missing;
    }
}
