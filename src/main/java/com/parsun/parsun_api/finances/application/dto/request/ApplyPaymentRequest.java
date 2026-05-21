package com.parsun.parsun_api.finances.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ApplyPaymentRequest(
        @NotEmpty(message = "At least one allocation is required")
        @Valid
        List<AllocationEntry> allocations
) {
    public record AllocationEntry(
            @NotNull(message = "Invoice is required")
            UUID invoiceId,

            @NotNull(message = "Amount is required")
            @Positive(message = "Amount must be greater than zero")
            BigDecimal amount
    ) {}
}
