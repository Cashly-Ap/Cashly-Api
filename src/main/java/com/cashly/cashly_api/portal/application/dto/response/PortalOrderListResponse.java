package com.cashly.cashly_api.portal.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PortalOrderListResponse(
    UUID id,
    String number,
    String orderNumber,
    LocalDateTime date,
    String status,
    BigDecimal total,
    BigDecimal subtotal,
    int items,
    int lineCount,
    LocalDateTime createdAt
) {}
