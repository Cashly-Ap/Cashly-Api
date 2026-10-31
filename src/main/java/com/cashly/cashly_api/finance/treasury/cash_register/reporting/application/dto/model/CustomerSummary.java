package com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model;

import java.math.BigDecimal;

public record CustomerSummary(String customerName, BigDecimal totalInvoiced,
        BigDecimal totalPayments, BigDecimal totalReturns, BigDecimal balance) {
}
