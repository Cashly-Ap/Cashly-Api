package com.cashly.cashly_api.invoices.application.api;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceDocumentRow(UUID id, String invoiceNumber, String customerName, String paymentType, int lineCount, String status, BigDecimal total, LocalDateTime createdAt) {}
