package com.parsun.parsun_api.portal.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PortalInvoiceItemResponse(
    UUID productId,
    String productCode,
    String productName,
    BigDecimal quantity,
    BigDecimal unitPrice,
    BigDecimal total
) {}
