package com.cashly.cashly_api.analytics.dashboard.application.dto.response;

import java.math.BigDecimal;

public record ProfitSummaryResponse(BigDecimal billing, BigDecimal expenses,
        BigDecimal net) {
}
