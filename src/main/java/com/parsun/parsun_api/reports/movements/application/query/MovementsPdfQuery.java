package com.parsun.parsun_api.reports.movements.application.query;

import java.time.LocalDate;
import java.util.UUID;

public record MovementsPdfQuery(
        UUID warehouseId,
        UUID productId,
        String movementType,
        LocalDate dateFrom,
        LocalDate dateTo) {}
