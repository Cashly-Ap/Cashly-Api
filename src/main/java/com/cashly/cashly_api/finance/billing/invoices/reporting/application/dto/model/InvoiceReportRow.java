package com.cashly.cashly_api.finance.billing.invoices.reporting.application.dto.model;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceReportRow(UUID id, String invoiceNumber,
        String customerName, String paymentType, int lineCount, String status,
        BigDecimal total, LocalDateTime createdAt) {
}
