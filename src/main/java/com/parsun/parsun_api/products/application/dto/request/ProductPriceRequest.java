package com.parsun.parsun_api.products.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductPriceRequest(
    @NotNull(message = "Price type ID is required")
    UUID priceTypeId,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    BigDecimal price
) {}
