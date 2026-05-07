package com.parsun.parsun_api.orders.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull(message = "Customer is required")
        UUID customerId,

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
            BigDecimal unitPrice,

            @NotEmpty(message = "At least one warehouse allocation is required")
            @Valid
            List<WarehouseAllocationRequest> warehouseAllocations
    ) {}

    public record WarehouseAllocationRequest(
            @NotNull(message = "Warehouse is required")
            UUID warehouseId,

            @NotNull(message = "Allocated quantity is required")
            @Min(value = 1, message = "Allocated quantity must be greater than zero")
            Integer allocated
    ) {}
}
