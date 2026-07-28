package com.parsun.parsun_api.portal.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PortalOrderDetailResponse(
    UUID id,
    String number,
    String orderNumber,
    LocalDateTime date,
    String status,
    BigDecimal total,
    BigDecimal subtotal,
    BigDecimal tax,
    String notes,
    String createdByName,
    LocalDateTime createdAt,
    String invoiceNumber,
    List<PortalOrderItemResponse> items,
    List<PortalOrderItemResponse> lines
) {}
