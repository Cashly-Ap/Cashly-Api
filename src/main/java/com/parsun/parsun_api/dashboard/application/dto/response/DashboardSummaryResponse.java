package com.parsun.parsun_api.dashboard.application.dto.response;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        BigDecimal monthlySales,
        long pendingOrders,
        BigDecimal overdueReceivables,
        long lowStockProducts
) {}
