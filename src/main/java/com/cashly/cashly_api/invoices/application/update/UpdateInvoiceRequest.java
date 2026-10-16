package com.cashly.cashly_api.invoices.application.update;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdateInvoiceRequest(
        @NotNull(message = "Payment type is required")
        String paymentType,

        @Size(max = 1000, message = "Notes must be at most 1000 characters")
        String notes,

        @NotEmpty(message = "At least one invoice line is required")
        @Valid
        List<InvoiceLineRequest> lines,

        LocalDate issueDate
) {
    public UpdateInvoiceRequest(String paymentType, String notes, List<InvoiceLineRequest> lines) {
        this(paymentType, notes, lines, null);
    }

    public record InvoiceLineRequest(
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
            BigDecimal allocated
    ) {}
}
