package com.cashly.cashly_api.finance.treasury.payables.application.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SupplierDebtSummaryResponse(UUID supplierId,
        String supplierName, String supplierRif, String supplierPhone,
        String supplierEmail, BigDecimal totalDebt,
        BigDecimal pendingPaymentsAmount, BigDecimal availableDebt,
        int creditDays, BigDecimal creditLimit, BigDecimal availableCredit,
        BigDecimal supplierBalance,
        List<SupplierPurchaseDebtResponse> purchasesWithDebt,
        long pendingPaymentsCount) {
}
