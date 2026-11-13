package com.cashly.cashly_api.analytics.dashboard.application.dto.response;

import java.math.BigDecimal;

public record DashboardSummaryResponse(BigDecimal monthlySales,
        BigDecimal monthlyExpenses, BigDecimal monthlyProfit,
        long pendingOrders, long pendingInvoices, long overdueInvoices,
        BigDecimal overdueReceivables, long lowStockProducts) {
}
