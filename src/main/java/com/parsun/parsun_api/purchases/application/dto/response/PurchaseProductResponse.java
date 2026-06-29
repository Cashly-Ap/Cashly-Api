package com.parsun.parsun_api.purchases.application.dto.response;

public record PurchaseProductResponse(
        String id,
        String code,
        String name,
        String categoryId,
        String categoryName,
        boolean active,
        int stock
) {}
