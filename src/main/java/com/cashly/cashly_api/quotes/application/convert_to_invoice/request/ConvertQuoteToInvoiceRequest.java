package com.cashly.cashly_api.quotes.application.convert_to_invoice.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ConvertQuoteToInvoiceRequest(
        @NotNull(message = "Payment type is required")
        String paymentType,

        String confirmationPassword,

        Boolean useBalance,

        BigDecimal balanceAmount,

        UUID paymentMethodId,

        LocalDate issueDate
) {
    public ConvertQuoteToInvoiceRequest(String paymentType, String confirmationPassword,
                                        Boolean useBalance, BigDecimal balanceAmount,
                                        UUID paymentMethodId) {
        this(paymentType, confirmationPassword, useBalance, balanceAmount, paymentMethodId, null);
    }
}
