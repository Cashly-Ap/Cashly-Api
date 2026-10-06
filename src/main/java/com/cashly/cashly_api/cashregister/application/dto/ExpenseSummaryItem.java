package com.cashly.cashly_api.cashregister.application.dto;

import java.math.BigDecimal;

public record ExpenseSummaryItem(
        String concept,
        BigDecimal total
) {}
