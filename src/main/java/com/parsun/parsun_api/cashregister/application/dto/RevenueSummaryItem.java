package com.parsun.parsun_api.cashregister.application.dto;

import java.math.BigDecimal;

public record RevenueSummaryItem(
        String paymentMethod,
        BigDecimal total
) {}
