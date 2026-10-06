package com.cashly.cashly_api.dashboard.application.dto.response;

import java.math.BigDecimal;

public record MonthlySalesResponse(
        String month,
        BigDecimal total
) {}
