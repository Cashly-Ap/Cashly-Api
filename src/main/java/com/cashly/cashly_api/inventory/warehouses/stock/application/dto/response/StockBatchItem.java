package com.cashly.cashly_api.inventory.warehouses.stock.application.dto.response;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record StockBatchItem(
        @NotNull(message = "Product ID is required") UUID productId,

        @NotNull(message = "Quantity is required") @DecimalMin(value = "0.0001", message = "Quantity must be at least 0.0001") BigDecimal quantity,

        @Size(max = 500, message = "Notes cannot exceed 500 characters") String notes) {
}
