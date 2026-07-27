package com.parsun.parsun_api.portal.application.dto.response;

import java.math.BigDecimal;

public record PortalProductResponse(
    String id,
    String code,
    String name,
    String description,
    String categoryId,
    String categoryName,
    boolean active,
    String photo,
    BigDecimal price,
    int availableStock
) {}
