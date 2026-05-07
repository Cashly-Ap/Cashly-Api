package com.parsun.parsun_api.orders.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateOrderRequest(
        @Size(max = 1000, message = "Notes must be at most 1000 characters")
        String notes,

        @NotEmpty(message = "At least one order line is required")
        @Valid
        List<OrderLineRequest> lines
) {
    public record OrderLineRequest(
            @NotNull(message = "Product is required")
            UUID productId,

            @NotNull(message = "Quantity is required")
            BigDecimal quantity,

            @NotNull(message = "Unit price is required")
            BigDecimal unitPrice
    ) {}
}
