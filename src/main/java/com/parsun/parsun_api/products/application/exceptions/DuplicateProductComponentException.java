package com.parsun.parsun_api.products.application.exceptions;

import java.util.UUID;

public class DuplicateProductComponentException extends RuntimeException {
    public DuplicateProductComponentException(UUID componentProductId) {
        super("Duplicate component in request: " + componentProductId);
    }
}
