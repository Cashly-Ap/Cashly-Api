package com.cashly.cashly_api.inventory.warehouses.stock.application.movements.model;

import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;

import java.util.UUID;

public record StockMovementFilter(UUID warehouseId, UUID productId,
        MovementType movementType, String search, int page, int size,
        StockMovementGroupBy groupBy) {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_SEARCH_LENGTH = 200;

    public StockMovementFilter(UUID warehouseId, UUID productId,
            MovementType movementType, int page, int size) {
        this(warehouseId, productId, movementType, null, page, size,
                StockMovementGroupBy.NONE);
    }

    public StockMovementFilter {
        groupBy = groupBy == null ? StockMovementGroupBy.NONE : groupBy;
        if (page < 0) {
            throw new IllegalArgumentException("Page must be zero or greater");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Size must be between 1 and " + MAX_PAGE_SIZE);
        }
        search = search == null ? "" : search.trim();
        if (search.length() > MAX_SEARCH_LENGTH) {
            throw new IllegalArgumentException("Search must not exceed "
                    + MAX_SEARCH_LENGTH + " characters");
        }
    }
}
