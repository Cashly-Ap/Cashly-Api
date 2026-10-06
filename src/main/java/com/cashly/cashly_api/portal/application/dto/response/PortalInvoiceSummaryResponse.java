package com.cashly.cashly_api.portal.application.dto.response;

import java.math.BigDecimal;

public record PortalInvoiceSummaryResponse(
        BigDecimal totalDebt,
        long pendingCount,
        BigDecimal paidThisMonth,
        BigDecimal pendingApprovalAmount
) {}
