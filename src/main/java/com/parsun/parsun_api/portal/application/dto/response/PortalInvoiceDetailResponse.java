package com.parsun.parsun_api.portal.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PortalInvoiceDetailResponse(
    UUID id,
    String number,
    String invoiceNumber,
    LocalDateTime date,
    LocalDateTime dueDate,
    String status,
    BigDecimal total,
    BigDecimal subtotal,
    BigDecimal tax,
    BigDecimal balance,
    BigDecimal paidAmount,
    String paymentType,
    String notes,
    LocalDateTime createdAt,
    List<PortalInvoiceItemResponse> items,
    List<PortalInvoiceItemResponse> lines
) {}
