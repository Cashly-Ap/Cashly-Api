package com.cashly.cashly_api.finance.billing.invoices.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceListResponse(UUID id, String invoiceNumber,
        UUID customerId, String customerName, String status, String statusLabel,
        String paymentType, BigDecimal subtotal, BigDecimal total,
        BigDecimal paidAmount, BigDecimal creditApplied,
        BigDecimal debitApplied, int lineCount, UUID orderId,
        String orderNumber, LocalDateTime createdAt, LocalDate issueDate,
        LocalDate dueDate, String returnStatus, boolean overdue) {
    public InvoiceListResponse(UUID id, String invoiceNumber, UUID customerId,
            String customerName, String status, String statusLabel,
            String paymentType, BigDecimal subtotal, BigDecimal total,
            BigDecimal paidAmount, BigDecimal creditApplied,
            BigDecimal debitApplied, int lineCount, UUID orderId,
            String orderNumber, LocalDateTime createdAt, String returnStatus) {
        this(id, invoiceNumber, customerId, customerName, status, statusLabel,
                paymentType, subtotal, total, paidAmount, creditApplied,
                debitApplied, lineCount, orderId, orderNumber, createdAt,
                createdAt.toLocalDate(), createdAt.toLocalDate(), returnStatus,
                false);
    }
}
