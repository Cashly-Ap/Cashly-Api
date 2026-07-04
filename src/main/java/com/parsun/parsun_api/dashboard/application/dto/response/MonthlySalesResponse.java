package com.parsun.parsun_api.dashboard.application.dto.response;

import java.math.BigDecimal;

public record MonthlySalesResponse(
        String month,
        BigDecimal total
) {}
