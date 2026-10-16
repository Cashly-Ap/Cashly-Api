package com.cashly.cashly_api.invoices.reporting.application.dto;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceReportRow(UUID id, String invoiceNumber, String customerName, String paymentType, int lineCount, String status, BigDecimal total, LocalDateTime createdAt) {}
