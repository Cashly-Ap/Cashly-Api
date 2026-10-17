package com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.query.model;

import java.time.LocalDate;
import java.util.UUID;

public record MovementsPdfQuery(
        UUID warehouseId,
        UUID productId,
        String movementType,
        LocalDate dateFrom,
        LocalDate dateTo) {}
