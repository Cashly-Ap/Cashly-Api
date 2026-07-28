package com.parsun.parsun_api.portal.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PortalOrderDetailResponse(
    UUID id,
    String number,
    LocalDateTime date,
    String status,
    BigDecimal total,
    BigDecimal subtotal,
    String notes,
    String createdByName,
    LocalDateTime createdAt,
    String invoiceNumber,
    List<PortalOrderItemResponse> items
) {}
