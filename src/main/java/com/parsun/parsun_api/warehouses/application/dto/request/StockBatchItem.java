package com.parsun.parsun_api.warehouses.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record StockBatchItem(
    @NotNull(message = "Product ID is required")
    UUID productId,

    @Min(value = 1, message = "Quantity must be at least 1")
    int quantity,

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    String notes
) {}
