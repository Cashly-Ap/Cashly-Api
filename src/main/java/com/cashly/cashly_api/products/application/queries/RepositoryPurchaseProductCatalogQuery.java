package com.cashly.cashly_api.products.application.queries;

import com.cashly.cashly_api.products.application.ports.in.PurchaseProductCatalogQuery;
import com.cashly.cashly_api.products.domain.ProductRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;
import java.util.UUID;

public class RepositoryPurchaseProductCatalogQuery implements PurchaseProductCatalogQuery {
    private final ProductRepository products;

    public RepositoryPurchaseProductCatalogQuery(ProductRepository products) {
        this.products = products;
    }

    @Override
    public PageResponse<Entry> find(String search, UUID categoryId, int page, int size) {
        var result = products.findAllWithFilters(search, categoryId, true, false, page, size, "name", "asc");
        var content = result.content().stream().map(product -> new Entry(product.getId(), product.getCode(),
                product.getName(), product.getCategoryId(), product.isActive(), product.getPurchasePrice())).toList();
        return new PageResponse<>(content, result.totalElements(), result.totalPages(), result.size(), result.number(),
                result.first(), result.last(), result.empty());
    }
}
