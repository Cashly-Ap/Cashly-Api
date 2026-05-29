package com.parsun.parsun_api.invoices.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvoiceListResponse(
        UUID id,
        String invoiceNumber,
        UUID customerId,
        String customerName,
        String status,
        String paymentType,
        BigDecimal subtotal,
        BigDecimal total,
        BigDecimal paidAmount,
        BigDecimal creditApplied,
        BigDecimal debitApplied,
        int lineCount,
        UUID orderId,
        String orderNumber,
        LocalDateTime createdAt,
        String returnStatus
) {}
