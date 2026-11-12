package com.cashly.cashly_api.finance.treasury.payables.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PayablesSummaryResponse(BigDecimal totalDebt,
        long totalSuppliersWithDebt, long totalPurchasesWithDebt,
        BigDecimal totalPendingValidationAmount,
        BigDecimal totalSupplierBalance, LocalDateTime calculatedAt) {
}
