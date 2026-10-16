package com.cashly.cashly_api.cash_register.sessions.application.dto;

import java.math.BigDecimal;

public record RevenueSummaryItem(
        String paymentMethod,
        BigDecimal total
) {}
