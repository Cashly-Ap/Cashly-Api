package com.parsun.parsun_api.receivables.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReceivablesSummaryResponse(
        BigDecimal totalDebt,
        long totalCustomersWithDebt,
        long totalInvoicesWithDebt,
        BigDecimal totalPendingValidationAmount,
        BigDecimal totalCustomerBalance,
        LocalDateTime calculatedAt
) {
    public static ReceivablesSummaryResponse empty() {
        return new ReceivablesSummaryResponse(
                BigDecimal.ZERO, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO, LocalDateTime.now()
        );
    }
}
