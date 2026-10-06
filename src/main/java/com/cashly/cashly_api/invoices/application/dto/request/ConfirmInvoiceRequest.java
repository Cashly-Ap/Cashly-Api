package com.cashly.cashly_api.invoices.application.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record ConfirmInvoiceRequest(
    String confirmationPassword,
    Boolean useBalance,
    BigDecimal balanceAmount,
    UUID paymentMethodId
) {}
