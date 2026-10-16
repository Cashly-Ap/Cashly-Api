package com.cashly.cashly_api.purchases.application.create;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreatePurchaseRequest(
        @NotNull(message = "Supplier is required")
        UUID supplierId,

        @Size(max = 1000, message = "Notes must be at most 1000 characters")
        String notes,

        @NotEmpty(message = "At least one purchase line is required")
        @Valid
        List<LineRequest> lines
) {
    public record LineRequest(
            @NotNull(message = "Product is required")
            UUID productId,

            @NotNull(message = "Quantity is required")
            BigDecimal quantity,

            @NotNull(message = "Unit cost is required")
            BigDecimal unitCost,

            @NotEmpty(message = "At least one warehouse allocation is required")
            @Valid
            List<AllocationRequest> warehouseAllocations
    ) {}

    public record AllocationRequest(
            @NotNull(message = "Warehouse is required")
            UUID warehouseId,

            @NotNull(message = "Allocated quantity is required")
            @Min(value = 1, message = "Allocated quantity must be greater than zero")
            BigDecimal quantity
    ) {}
}
