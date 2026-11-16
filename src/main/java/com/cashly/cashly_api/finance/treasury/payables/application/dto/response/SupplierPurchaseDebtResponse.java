package com.cashly.cashly_api.finance.treasury.payables.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SupplierPurchaseDebtResponse(UUID purchaseId,
        String purchaseNumber, String status, String paymentType,
        LocalDate issueDate, LocalDate dueDate, BigDecimal subtotal,
        BigDecimal returnedTotal, BigDecimal totalPaid, BigDecimal total,
        BigDecimal balance, UUID pendingPaymentId,
        String pendingPaymentStatus) {
}
