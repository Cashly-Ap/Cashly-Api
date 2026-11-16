package com.cashly.cashly_api.analytics.dashboard.application.dto.response;

import java.math.BigDecimal;

public record TopItemResponse(String id, String name, BigDecimal total) {
}
