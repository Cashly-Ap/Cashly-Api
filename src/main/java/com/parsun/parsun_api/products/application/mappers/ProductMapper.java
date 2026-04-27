package com.parsun.parsun_api.products.application.mappers;

import com.parsun.parsun_api.products.application.dto.response.ProductDetailResponse;
import com.parsun.parsun_api.products.application.dto.response.ProductDetailResponse.ComponentResponse;
import com.parsun.parsun_api.products.application.dto.response.ProductListResponse;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.entities.ProductComponent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProductMapper {

    public ProductListResponse toListResponse(Product product, String categoryName) {
        return new ProductListResponse(
            product.getId().toString(), product.getCode(), product.getName(),
            product.getCategoryId().toString(), categoryName,
            product.isComposite(), product.isActive()
        );
    }

    public ProductDetailResponse toDetailResponse(Product product, String categoryName,
                                                   Map<UUID, Product> componentProducts) {
        List<ComponentResponse> components = product.getComponents().stream()
            .map(c -> {
                Product comp = componentProducts.get(c.getComponentProductId());
                String compCode = comp != null ? comp.getCode() : "";
                String compName = comp != null ? comp.getName() : "";
                String compId = c.getId() != null ? c.getId().toString() : null;
                return new ComponentResponse(compId, c.getComponentProductId().toString(), compCode, compName, c.getQuantity());
            })
            .toList();

        return new ProductDetailResponse(
            product.getId().toString(), product.getCode(), product.getName(), product.getDescription(),
            product.getCategoryId().toString(), categoryName,
            product.isComposite(), product.isActive(),
            product.getBasePrice(), product.getPurchasePrice(), product.getProfitMargin(),
            components, product.getCreatedAt(), product.getUpdatedAt()
        );
    }
}
