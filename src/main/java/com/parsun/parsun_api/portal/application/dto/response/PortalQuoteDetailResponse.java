package com.parsun.parsun_api.portal.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PortalQuoteDetailResponse(
    UUID id,
    String number,
    LocalDate date,
    String status,
    BigDecimal total,
    BigDecimal subtotal,
    String notes,
    LocalDate expiryDate,
    String orderNumber,
    String createdByName,
    Instant createdAt,
    List<PortalQuoteItemResponse> items
) {}
