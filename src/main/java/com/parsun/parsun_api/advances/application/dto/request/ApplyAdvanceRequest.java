package com.parsun.parsun_api.advances.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record ApplyAdvanceRequest(
        @NotNull(message = "Invoice is required")
        UUID invoiceId,
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount
) {}
