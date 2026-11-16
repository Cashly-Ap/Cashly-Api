package com.cashly.cashly_api.sales.quotes.application.create.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateQuoteRequest(
        @NotNull(message = "Customer is required") UUID customerId,

        LocalDate expiryDate,

        @Size(max = 1000, message = "Notes must be at most 1000 characters") String notes,

        @NotEmpty(message = "At least one quote line is required") @Valid List<QuoteLineRequest> lines) {
    public record QuoteLineRequest(
            @NotNull(message = "Product is required") UUID productId,

            @NotNull(message = "Quantity is required") @DecimalMin(value = "0.0001", message = "Quantity must be greater than zero") BigDecimal quantity,

            @NotNull(message = "Unit price is required") @DecimalMin(value = "0.01", message = "Unit price must be greater than zero") BigDecimal unitPrice,

            @Valid List<WarehouseAllocationRequest> warehouseAllocations) {
    }

    public record WarehouseAllocationRequest(
            @NotNull(message = "Warehouse is required") UUID warehouseId,

            @NotNull(message = "Allocated quantity is required") @DecimalMin(value = "0.0001", message = "Allocated quantity must be greater than zero") BigDecimal allocatedQuantity) {
    }
}
