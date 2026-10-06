package com.cashly.cashly_api.portal.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PortalPaymentListResponse(
    UUID id,
    String number,
    String paymentNumber,
    LocalDate date,
    LocalDate paymentDate,
    String method,
    String status,
    BigDecimal amount,
    BigDecimal appliedAmount,
    String reference,
    String referenceNumber,
    LocalDateTime createdAt
) {}
