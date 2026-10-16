package com.cashly.cashly_api.payments.collection.application.pay_customer_debt.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PayCustomerDebtRequest(
        UUID paymentMethodId,

        @Size(max = 100, message = "Reference number must be at most 100 characters")
        String referenceNumber,

        @NotNull(message = "Payment date is required")
        LocalDate paymentDate,

        Boolean useBalance,

        BigDecimal balanceAmount
) {}
