package com.cashly.cashly_api.inventory.warehouses.stock.application.queries.model;

import java.util.UUID;

public record StockOverviewFilter(UUID warehouseId, UUID categoryId,
        String search, int page, int size, StockOverviewGroupBy groupBy) {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_SEARCH_LENGTH = 200;

    public StockOverviewFilter(UUID warehouseId, UUID categoryId, String search,
            int page, int size) {
        this(warehouseId, categoryId, search, page, size,
                StockOverviewGroupBy.NONE);
    }

    public StockOverviewFilter {
        groupBy = groupBy == null ? StockOverviewGroupBy.NONE : groupBy;
        if (page < 0)
            throw new IllegalArgumentException("Page must be zero or greater");
        if (size < 1 || size > MAX_PAGE_SIZE)
            throw new IllegalArgumentException(
                    "Size must be between 1 and " + MAX_PAGE_SIZE);
        search = search == null ? "" : search.trim();
        if (search.length() > MAX_SEARCH_LENGTH)
            throw new IllegalArgumentException("Search must not exceed "
                    + MAX_SEARCH_LENGTH + " characters");
    }
}
