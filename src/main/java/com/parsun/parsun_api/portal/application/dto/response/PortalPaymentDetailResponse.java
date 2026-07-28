package com.parsun.parsun_api.portal.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PortalPaymentDetailResponse(
    UUID id,
    String number,
    LocalDate date,
    String method,
    String status,
    BigDecimal amount,
    BigDecimal appliedAmount,
    String reference,
    String referenceNumber,
    String notes,
    LocalDateTime createdAt,
    List<PortalPaymentInvoiceResponse> invoices
) {}
