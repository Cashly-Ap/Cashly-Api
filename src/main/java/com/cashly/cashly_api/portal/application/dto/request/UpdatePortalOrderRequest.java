package com.cashly.cashly_api.portal.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdatePortalOrderRequest(
        @Size(max = 1000, message = "Notes must be at most 1000 characters")
        String notes,

        @NotEmpty(message = "At least one order line is required")
        @Valid
        List<PortalOrderLineRequest> lines
) {
    public record PortalOrderLineRequest(
            @NotNull(message = "Product is required")
            UUID productId,

            @NotNull(message = "Quantity is required")
            @DecimalMin(value = "0.0001", message = "Quantity must be greater than zero")
            BigDecimal quantity,

            @NotNull(message = "Unit price is required")
            @DecimalMin(value = "0.01", message = "Unit price must be greater than zero")
            BigDecimal unitPrice
    ) {}
}
