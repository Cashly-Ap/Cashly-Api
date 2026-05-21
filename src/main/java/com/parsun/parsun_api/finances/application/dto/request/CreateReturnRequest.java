package com.parsun.parsun_api.finances.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateReturnRequest(
        @NotNull(message = "Invoice is required")
        UUID invoiceId,

        @NotNull(message = "Customer is required")
        UUID customerId,

        @NotNull(message = "Return type is required")
        String returnType,

        @NotBlank(message = "Reason is required")
        String reason,

        List<ReturnLineRequest> lines
) {
    public record ReturnLineRequest(
            @NotNull(message = "Invoice line is required")
            UUID invoiceLineId,

            @NotNull(message = "Product is required")
            UUID productId,

            @NotNull(message = "Quantity is required")
            @Positive(message = "Quantity must be greater than zero")
            BigDecimal quantity,

            @NotNull(message = "Warehouse is required")
            UUID warehouseId,

            @NotNull(message = "Unit price is required")
            BigDecimal unitPrice
    ) {}
}
