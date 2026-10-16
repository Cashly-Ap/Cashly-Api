package com.cashly.cashly_api.quotes.application.assign_warehouses;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AssignQuoteWarehousesRequest(
    @NotEmpty(message = "At least one line allocation is required")
    @Valid
    List<LineAllocationRequest> lines
) {
    public record LineAllocationRequest(
        @NotNull(message = "Line id is required")
        UUID lineId,

        @NotEmpty(message = "At least one warehouse allocation is required")
        @Valid
        List<WarehouseAllocationRequest> allocations
    ) {}

    public record WarehouseAllocationRequest(
        @NotNull(message = "Warehouse is required")
        UUID warehouseId,

        @NotNull(message = "Allocated quantity is required")
        @DecimalMin(value = "0.0001", message = "Allocated quantity must be greater than zero")
        BigDecimal allocatedQuantity
    ) {}
}
