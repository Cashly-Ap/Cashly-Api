package com.parsun.parsun_api.portal.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PortalInvoiceListResponse(
    UUID id,
    String number,
    LocalDateTime date,
    LocalDateTime dueDate,
    String status,
    BigDecimal total,
    BigDecimal balance,
    BigDecimal paidAmount,
    LocalDateTime createdAt
) {}
