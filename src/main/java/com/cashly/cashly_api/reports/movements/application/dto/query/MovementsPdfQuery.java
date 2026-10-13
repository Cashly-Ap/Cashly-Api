package com.cashly.cashly_api.reports.movements.application.dto.query;

import java.time.LocalDate;
import java.util.UUID;

public record MovementsPdfQuery(
        UUID warehouseId,
        UUID productId,
        String movementType,
        LocalDate dateFrom,
        LocalDate dateTo) {}
