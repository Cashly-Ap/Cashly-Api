package com.cashly.cashly_api.quotes.reporting.application.dto;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;

public record QuoteReportData(
        UUID id, String quoteNumber, String customerName, String customerRif, String status,
        String notes, String cancelledReason, Instant createdAt, BigDecimal subtotal, List<Line> lines) {
    public QuoteReportData { lines = List.copyOf(lines); }
    public record Line(String productCode, String productName, BigDecimal quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}
}
