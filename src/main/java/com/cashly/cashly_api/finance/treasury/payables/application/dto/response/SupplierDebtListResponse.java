package com.cashly.cashly_api.finance.treasury.payables.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SupplierDebtListResponse(UUID supplierId, String supplierName,
        String supplierRif, BigDecimal totalDebt,
        BigDecimal pendingPaymentsAmount, long pendingPurchasesCount,
        LocalDate oldestDueDate, int creditDays, BigDecimal creditLimit,
        BigDecimal availableCredit, BigDecimal supplierBalance) {
}
