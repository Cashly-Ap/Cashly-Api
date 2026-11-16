package com.cashly.cashly_api.sales.invoices.invoices.application.issue.request;

import java.math.BigDecimal;
import java.util.UUID;

public record IssueInvoiceCommand(String confirmationPassword,
        Boolean useBalance, BigDecimal balanceAmount, UUID paymentMethodId) {
}
