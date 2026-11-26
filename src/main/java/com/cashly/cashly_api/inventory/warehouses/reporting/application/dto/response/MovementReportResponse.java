package com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MovementReportResponse(UUID movementId, String warehouseName,
        String productName, String sku, String movementType,
        BigDecimal quantity, BigDecimal previousStock, BigDecimal newStock,
        String notes, LocalDateTime createdAt) {
}
