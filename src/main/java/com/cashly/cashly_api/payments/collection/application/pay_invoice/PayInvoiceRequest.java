package com.cashly.cashly_api.payments.collection.application.pay_invoice;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PayInvoiceRequest(
        UUID paymentMethodId,

        @Size(max = 100, message = "Reference number must be at most 100 characters")
        String referenceNumber,

        @NotNull(message = "Payment date is required")
        LocalDate paymentDate,

        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

        Boolean useBalance,

        BigDecimal balanceAmount
) {}
