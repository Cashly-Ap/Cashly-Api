package com.cashly.cashly_api.finance.billing.invoices.application.confirm.request;

import java.math.BigDecimal;
import java.util.UUID;

public record ConfirmInvoiceRequest(
    String confirmationPassword,
    Boolean useBalance,
    BigDecimal balanceAmount,
    UUID paymentMethodId
) {}
