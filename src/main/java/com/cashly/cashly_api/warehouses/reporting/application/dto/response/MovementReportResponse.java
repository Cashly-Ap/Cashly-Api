package com.cashly.cashly_api.warehouses.reporting.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record MovementReportResponse(
        UUID movementId,
        String warehouseName,
        String productName,
        String sku,
        String movementType,
        int quantity,
        int previousStock,
        int newStock,
        String notes,
        LocalDateTime createdAt) {}
