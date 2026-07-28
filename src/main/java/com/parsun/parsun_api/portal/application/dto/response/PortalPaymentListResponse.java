package com.parsun.parsun_api.portal.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PortalPaymentListResponse(
    UUID id,
    String number,
    LocalDate date,
    String method,
    String status,
    BigDecimal amount,
    String reference,
    LocalDateTime createdAt
) {}
