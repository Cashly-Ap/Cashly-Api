package com.cashly.cashly_api.categories.application.exceptions;

import java.util.UUID;

public class CategoryHasActiveProductsException extends RuntimeException {
    public CategoryHasActiveProductsException(UUID categoryId) {
        super("Cannot deactivate category with id " + categoryId + " because it has active products");
    }
}
