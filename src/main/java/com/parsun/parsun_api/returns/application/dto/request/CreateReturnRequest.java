package com.parsun.parsun_api.returns.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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

        @NotNull(message = "Reason is required")
        @Size(max = 500, message = "Reason must be at most 500 characters")
        String reason,

        @NotEmpty(message = "At least one return line is required")
        @Valid
        List<ReturnLineRequest> lines
) {
    public record ReturnLineRequest(

            @NotNull(message = "Invoice line is required")
            UUID invoiceLineId,

            @NotNull(message = "Product is required")
            UUID productId,

            @NotNull(message = "Quantity is required")
            BigDecimal quantity,

            @NotNull(message = "Unit price is required")
            BigDecimal unitPrice,

            @NotNull(message = "Subtotal is required")
            BigDecimal subtotal,

            @Size(max = 500, message = "Line reason must be at most 500 characters")
            String reason,

            @NotEmpty(message = "At least one allocation is required")
            @Valid
            List<AllocationRequest> allocations
    ) {
        public record AllocationRequest(
                @NotNull(message = "Warehouse is required")
                UUID warehouseId,

                @NotNull(message = "Allocated quantity is required")
                BigDecimal allocated
        ) {}
    }
}
