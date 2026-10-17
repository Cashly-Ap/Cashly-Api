package com.cashly.cashly_api.finance.billing.invoices.application.ports.in;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceDocumentSnapshot(
        UUID id, String invoiceNumber, String customerName, String customerRif, String orderNumber,
        String status, String notes, String cancelledReason, LocalDateTime createdAt, BigDecimal subtotal,
        String paymentType, BigDecimal total, List<Line> lines) {
    public InvoiceDocumentSnapshot {
        lines = List.copyOf(lines);
    }

    public record Line(String productCode, String productName, BigDecimal quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}
}

