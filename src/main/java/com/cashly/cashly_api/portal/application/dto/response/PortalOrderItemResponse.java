package com.cashly.cashly_api.portal.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PortalOrderItemResponse(
    UUID productId,
    String productCode,
    String productName,
    BigDecimal quantity,
    BigDecimal unitPrice,
    BigDecimal total
) {}
