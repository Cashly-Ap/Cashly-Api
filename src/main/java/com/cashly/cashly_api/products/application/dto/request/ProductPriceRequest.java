package com.cashly.cashly_api.products.application.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductPriceRequest(
    @NotNull(message = "Price type ID is required")
    UUID priceTypeId,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    BigDecimal price,

    @DecimalMin(value = "0.0", message = "Profit margin must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Profit margin must be between 0 and 100")
    BigDecimal profitMargin
) {}
