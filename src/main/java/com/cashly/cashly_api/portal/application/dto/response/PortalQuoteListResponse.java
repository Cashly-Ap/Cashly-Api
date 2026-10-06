package com.cashly.cashly_api.portal.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PortalQuoteListResponse(
    UUID id,
    String number,
    String quoteNumber,
    LocalDate date,
    LocalDate expiryDate,
    String status,
    BigDecimal total,
    BigDecimal subtotal,
    int items,
    Instant createdAt
) {}
