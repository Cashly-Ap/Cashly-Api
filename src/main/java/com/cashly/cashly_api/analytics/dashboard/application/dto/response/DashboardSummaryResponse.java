package com.cashly.cashly_api.analytics.dashboard.application.dto.response;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        BigDecimal monthlySales,
        long pendingOrders,
        BigDecimal overdueReceivables,
        long lowStockProducts
) {}
