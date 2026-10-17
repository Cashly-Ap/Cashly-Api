package com.cashly.cashly_api.procurement.purchases.infrastructure.integration;

import com.cashly.cashly_api.catalog.categories.application.ports.in.ProductCategoryQuery;
import com.cashly.cashly_api.catalog.products.application.ports.in.PurchaseProductCatalogQuery;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseProductCatalogGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class LocalPurchaseProductCatalogGateway implements PurchaseProductCatalogGateway {
    private final PurchaseProductCatalogQuery products;
    private final ProductCategoryQuery categories;

    public LocalPurchaseProductCatalogGateway(PurchaseProductCatalogQuery products, ProductCategoryQuery categories) {
        this.products = products;
        this.categories = categories;
    }

    @Override
    public PageResponse<Entry> find(String search, UUID categoryId, int page, int size) {
        var result = products.find(search, categoryId, page, size);
        var content = result.content().stream().map(product -> new Entry(product.id(), product.code(), product.name(),
                product.categoryId(), categories.findById(product.categoryId()).map(ProductCategoryQuery.Entry::name)
                        .orElse(""), product.active(), product.purchasePrice())).toList();
        return new PageResponse<>(content, result.totalElements(), result.totalPages(), result.size(), result.number(),
                result.first(), result.last(), result.empty());
    }
}
