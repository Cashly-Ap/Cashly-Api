package com.cashly.cashly_api.orders.application.api;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderDocumentRow(UUID id, String orderNumber, String customerName, String status, BigDecimal subtotal, LocalDateTime createdAt, int lineCount, UUID invoiceId) {}
