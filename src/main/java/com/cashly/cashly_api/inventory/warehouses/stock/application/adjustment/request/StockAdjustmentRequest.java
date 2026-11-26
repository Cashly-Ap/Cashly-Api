package com.cashly.cashly_api.inventory.warehouses.stock.application.adjustment.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record StockAdjustmentRequest(
        @NotNull(message = "Product ID is required") UUID productId,

        @NotNull(message = "New quantity is required") @DecimalMin(value = "0.0", message = "New quantity cannot be negative") BigDecimal newQuantity,

        @Size(max = 500, message = "Notes cannot exceed 500 characters") String notes) {
}
