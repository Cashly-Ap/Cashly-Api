package com.cashly.cashly_api.payments.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreatePaymentRequest(

        @NotNull(message = "Customer is required")
        UUID customerId,

        @NotNull(message = "Payment status is required")
        @JsonAlias("paymentStatus")
        String status,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Payment method is required")
        UUID paymentMethodId,

        String referenceNumber,

        @NotNull(message = "Payment date is required")
        LocalDate paymentDate,

        @Size(max = 1000, message = "Notes must be at most 1000 characters")
        String notes

) {}
