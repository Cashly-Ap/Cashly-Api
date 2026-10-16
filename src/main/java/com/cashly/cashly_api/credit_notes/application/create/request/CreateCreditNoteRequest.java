package com.cashly.cashly_api.credit_notes.application.create.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateCreditNoteRequest(
        @NotNull(message = "Customer is required")
        UUID customerId,

        @NotNull(message = "Invoice is required")
        UUID invoiceId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Reason is required")
        @Size(max = 500, message = "Reason must be at most 500 characters")
        String reason
) {}
