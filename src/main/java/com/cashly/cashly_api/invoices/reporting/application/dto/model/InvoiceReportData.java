package com.cashly.cashly_api.invoices.reporting.application.dto.model;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceReportData(
        UUID id, String invoiceNumber, String customerName, String customerRif, String orderNumber,
        String status, String notes, String cancelledReason, LocalDateTime createdAt, BigDecimal subtotal,
        String paymentType, BigDecimal total, List<Line> lines) {
    public InvoiceReportData {
        lines = List.copyOf(lines);
    }

    public record Line(String productCode, String productName, BigDecimal quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}
}

