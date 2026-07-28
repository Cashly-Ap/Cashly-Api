package com.parsun.parsun_api.portal.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PortalOrderListResponse(
    UUID id,
    String number,
    LocalDateTime date,
    String status,
    BigDecimal total,
    int items,
    String customerName,
    LocalDateTime createdAt
) {}
