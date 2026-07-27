package com.parsun.parsun_api.portal.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record PortalDashboardResponse(
    long draftQuotes,
    long confirmedOrders,
    long pendingInvoices,
    BigDecimal pendingAmount,
    List<RecentActivityItem> recentActivity
) {
    public record RecentActivityItem(
        String type,
        String id,
        String number,
        String status,
        BigDecimal amount,
        String date
    ) {}
}
