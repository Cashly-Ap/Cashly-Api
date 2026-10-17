package com.cashly.cashly_api.catalog.products.application.exceptions;

import java.util.UUID;

public class CompositeProductCannotBeComponentException extends RuntimeException {
    public CompositeProductCannotBeComponentException(UUID id) {
        super("Composite product with id " + id + " cannot be used as a component");
    }
}
