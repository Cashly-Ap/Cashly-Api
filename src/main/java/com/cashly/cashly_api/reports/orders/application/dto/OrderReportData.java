package com.cashly.cashly_api.reports.orders.application.dto;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderReportData(
        UUID id, String orderNumber, String customerName, String customerRif, String status,
        String notes, String cancelledReason, LocalDateTime createdAt, BigDecimal subtotal, List<Line> lines) {
    public OrderReportData { lines = List.copyOf(lines); }
    public record Line(String productCode, String productName, BigDecimal quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}
}
