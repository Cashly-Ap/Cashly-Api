package com.parsun.parsun_api.products.application.dto.response;

public record ProductListResponse(
    String id,
    String code,
    String name,
    String categoryId,
    String categoryName,
    boolean composite,
    boolean active
) {}
