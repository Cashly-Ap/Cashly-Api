package com.parsun.parsun_api.products.application.mappers;

import com.parsun.parsun_api.products.application.dto.response.ProductDetailResponse;
import com.parsun.parsun_api.products.application.dto.response.ProductDetailResponse.ComponentResponse;
import com.parsun.parsun_api.products.application.dto.response.ProductListResponse;
import com.parsun.parsun_api.products.application.dto.response.ProductListResponse.ProductPriceListItem;
import com.parsun.parsun_api.products.application.dto.response.ProductPriceDetailResponse;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.valueobjects.ProductPrice;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProductMapper {

    public ProductListResponse toListResponse(Product product, String categoryName, boolean deletable) {
        List<ProductPriceListItem> prices = product.getProductPrices().stream()
            .map(pp -> new ProductPriceListItem(pp.getPriceTypeId().toString(), pp.getPrice()))
            .toList();
        return new ProductListResponse(
            product.getId().toString(), product.getCode(), product.getName(),
            product.getCategoryId().toString(), categoryName,
            product.isComposite(), product.isActive(),
            product.getPurchasePrice(), prices, deletable
        );
    }

    public ProductDetailResponse toDetailResponse(Product product, String categoryName,
                                                   Map<UUID, Product> componentProducts,
                                                   Map<UUID, String> priceTypeNames) {
        List<ComponentResponse> components = product.getComponents().stream()
            .map(c -> {
                Product comp = componentProducts.get(c.getComponentProductId());
                String compCode = comp != null ? comp.getCode() : "";
                String compName = comp != null ? comp.getName() : "";
                String compId = c.getId() != null ? c.getId().toString() : null;
                return new ComponentResponse(compId, c.getComponentProductId().toString(), compCode, compName, c.getQuantity());
            })
            .toList();

        List<ProductPriceDetailResponse> productPrices = product.getProductPrices().stream()
            .map(pp -> {
                String priceId = pp.getId() != null ? pp.getId().toString() : null;
                String priceTypeName = priceTypeNames.getOrDefault(pp.getPriceTypeId(), "");
                return new ProductPriceDetailResponse(priceId, pp.getPriceTypeId().toString(), priceTypeName, pp.getPrice(), pp.getProfitMargin());
            })
            .toList();

        return new ProductDetailResponse(
            product.getId().toString(), product.getCode(), product.getName(), product.getDescription(),
            product.getCategoryId().toString(), categoryName,
            product.isComposite(), product.isActive(),
            product.getPhoto(),
            product.getPurchasePrice(),
            components, productPrices, product.getCreatedAt(), product.getUpdatedAt()
        );
    }
}
