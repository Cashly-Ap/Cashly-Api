package com.parsun.parsun_api.warehouses.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record StockAdjustmentRequest(
        @NotNull(message = "Product ID is required")
        UUID productId,

        @Min(value = 0, message = "New quantity cannot be negative")
        int newQuantity,

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        String notes
) {}
