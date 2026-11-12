package com.cashly.cashly_api.procurement.purchases.application.update.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdatePurchaseRequest(
        @Size(max = 1000, message = "Notes must be at most 1000 characters") String notes,

        String paymentType,

        LocalDate issueDate,

        LocalDate dueDate,

        @NotEmpty(message = "At least one purchase line is required") @Valid List<LineRequest> lines) {

    public UpdatePurchaseRequest(String notes, List<LineRequest> lines) {
        this(notes, null, null, null, lines);
    }

    public record LineRequest(
            @NotNull(message = "Product is required") UUID productId,

            @NotNull(message = "Quantity is required") BigDecimal quantity,

            @NotNull(message = "Unit cost is required") BigDecimal unitCost,

            @NotEmpty(message = "At least one warehouse allocation is required") @Valid List<AllocationRequest> warehouseAllocations) {
    }

    public record AllocationRequest(
            @NotNull(message = "Warehouse is required") UUID warehouseId,

            @NotNull(message = "Allocated quantity is required") BigDecimal quantity) {
    }
}
