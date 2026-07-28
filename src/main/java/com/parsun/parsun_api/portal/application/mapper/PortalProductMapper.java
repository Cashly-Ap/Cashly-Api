package com.parsun.parsun_api.portal.application.mapper;

import com.parsun.parsun_api.categories.domain.entities.Category;
import com.parsun.parsun_api.portal.application.dto.response.PortalProductResponse;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.valueobjects.ProductPrice;

import java.math.BigDecimal;

public final class PortalProductMapper {

    private PortalProductMapper() {}

    public static PortalProductResponse toResponse(Product product, Category category, int stock, int availableStock) {
        BigDecimal price = BigDecimal.ZERO;
        if (product.getProductPrices() != null && !product.getProductPrices().isEmpty()) {
            price = product.getProductPrices().getFirst().getPrice();
        }

        return new PortalProductResponse(
            product.getId(),
            product.getCode(),
            product.getName(),
            product.getDescription(),
            price,
            stock,
            availableStock,
            "unidad",
            product.getCategoryId(),
            category != null ? category.getName() : null,
            product.isActive()
        );
    }
}
