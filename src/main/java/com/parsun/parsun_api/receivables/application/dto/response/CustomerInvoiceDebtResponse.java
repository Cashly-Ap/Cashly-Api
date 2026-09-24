package com.parsun.parsun_api.receivables.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CustomerInvoiceDebtResponse(
        UUID invoiceId,
        String invoiceNumber,
        String status,
        LocalDate invoiceDate,
        LocalDate dueDate,
        BigDecimal subtotal,
        BigDecimal returnedTotal,
        BigDecimal totalPaid,
        BigDecimal creditApplied,
        BigDecimal debitApplied,
        BigDecimal total,
        BigDecimal balance,
        UUID pendingPaymentId,
        String pendingPaymentStatus
) {
    public CustomerInvoiceDebtResponse(UUID invoiceId, String invoiceNumber, String status,
                                       LocalDate invoiceDate, BigDecimal subtotal,
                                       BigDecimal returnedTotal, BigDecimal totalPaid,
                                       BigDecimal creditApplied, BigDecimal debitApplied,
                                       BigDecimal total, BigDecimal balance,
                                       UUID pendingPaymentId, String pendingPaymentStatus) {
        this(invoiceId, invoiceNumber, status, invoiceDate, invoiceDate, subtotal, returnedTotal,
                totalPaid, creditApplied, debitApplied, total, balance, pendingPaymentId,
                pendingPaymentStatus);
    }
}
