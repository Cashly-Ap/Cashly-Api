package com.parsun.parsun_api.portal.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PortalQuoteListResponse(
    UUID id,
    String number,
    LocalDate date,
    String status,
    BigDecimal total,
    int items,
    Instant createdAt
) {}
