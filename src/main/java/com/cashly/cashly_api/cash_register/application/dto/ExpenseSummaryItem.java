package com.cashly.cashly_api.cash_register.application.dto;

import java.math.BigDecimal;

public record ExpenseSummaryItem(
        String concept,
        BigDecimal total
) {}
