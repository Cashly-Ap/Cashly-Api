package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.web;

import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.model.StockMovementFilter;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.model.StockMovementGroupBy;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;

import java.util.Locale;
import java.util.UUID;

record StockMovementsQuery(String warehouseId, String productId,
        String movementType, String search, String page, String size,
        String groupBy) {

    static final int DEFAULT_PAGE = 0;
    static final int DEFAULT_SIZE = 20;

    StockMovementFilter toFilter() {
        return new StockMovementFilter(uuid("warehouseId", warehouseId),
                uuid("productId", productId),
                movementType("movementType", movementType), search,
                integer("page", page, DEFAULT_PAGE),
                integer("size", size, DEFAULT_SIZE),
                groupBy("groupBy", groupBy));
    }

    private static UUID uuid(String parameter, String raw) {
        String value = text(raw);
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw invalid(parameter, value, "UUID");
        }
    }

    private static MovementType movementType(String parameter, String raw) {
        String value = text(raw);
        if (value == null) {
            return null;
        }
        try {
            return MovementType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw invalid(parameter, value,
                    "movement type (" + enumNames(MovementType.values()) + ")");
        }
    }

    private static StockMovementGroupBy groupBy(String parameter, String raw) {
        String value = text(raw);
        if (value == null) {
            return StockMovementGroupBy.NONE;
        }
        try {
            return StockMovementGroupBy
                    .valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw invalid(parameter, value, "grouping ("
                    + enumNames(StockMovementGroupBy.values()) + ")");
        }
    }

    private static int integer(String parameter, String raw, int defaultValue) {
        String value = text(raw);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw invalid(parameter, value, "whole number");
        }
    }

    private static String text(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        return value.isEmpty() ? null : value;
    }

    private static IllegalArgumentException invalid(String parameter,
            String value, String expected) {
        return new IllegalArgumentException("Invalid value '" + value
                + "' for parameter '" + parameter + "'. Expected a valid "
                + expected + ".");
    }

    private static String enumNames(Enum<?>[] values) {
        StringBuilder names = new StringBuilder();
        for (Enum<?> value : values) {
            if (!names.isEmpty()) {
                names.append(", ");
            }
            names.append(value.name());
        }
        return names.toString();
    }
}
