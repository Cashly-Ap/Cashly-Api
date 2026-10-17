package com.cashly.cashly_api.sales.orders.reporting.application.dto.model;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderReportRow(UUID id, String orderNumber, String customerName, String status, BigDecimal subtotal, LocalDateTime createdAt, int lineCount, UUID invoiceId) {}
