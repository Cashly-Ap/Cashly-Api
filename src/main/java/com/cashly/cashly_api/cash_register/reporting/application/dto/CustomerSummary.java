package com.cashly.cashly_api.cash_register.reporting.application.dto;

import java.math.BigDecimal;

public record CustomerSummary(
        String customerName,
        BigDecimal totalInvoiced,
        BigDecimal totalPayments,
        BigDecimal totalReturns,
        BigDecimal balance
) {}
