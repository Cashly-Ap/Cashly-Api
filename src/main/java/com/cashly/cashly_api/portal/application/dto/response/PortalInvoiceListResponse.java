package com.cashly.cashly_api.portal.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PortalInvoiceListResponse(
    UUID id,
    String number,
    String invoiceNumber,
    LocalDateTime date,
    LocalDateTime dueDate,
    String status,
    BigDecimal total,
    BigDecimal subtotal,
    BigDecimal balance,
    BigDecimal paidAmount,
    String paymentType,
    LocalDateTime createdAt,
    BigDecimal pendingApprovalAmount
) {}
