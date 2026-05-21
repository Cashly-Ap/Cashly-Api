package com.parsun.parsun_api.finances.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterPaymentRequest(
        @NotNull(message = "Customer is required")
        UUID customerId,

        @NotNull(message = "Payment type is required")
        String type,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Payment method is required")
        UUID paymentMethodId,

        @Size(max = 100, message = "Reference number must be at most 100 characters")
        String referenceNumber,

        @NotNull(message = "Payment date is required")
        LocalDate paymentDate,

        @Size(max = 500, message = "Notes must be at most 500 characters")
        String notes
) {}
