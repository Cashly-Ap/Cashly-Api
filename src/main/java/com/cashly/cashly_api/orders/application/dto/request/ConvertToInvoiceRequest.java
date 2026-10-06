package com.cashly.cashly_api.orders.application.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ConvertToInvoiceRequest(
        @NotNull(message = "Payment type is required")
        String paymentType,

        String confirmationPassword,

        Boolean useBalance,

        BigDecimal balanceAmount,

        UUID paymentMethodId,

        LocalDate issueDate
) {
    public ConvertToInvoiceRequest(String paymentType, String confirmationPassword,
                                   Boolean useBalance, BigDecimal balanceAmount,
                                   UUID paymentMethodId) {
        this(paymentType, confirmationPassword, useBalance, balanceAmount, paymentMethodId, null);
    }
}
