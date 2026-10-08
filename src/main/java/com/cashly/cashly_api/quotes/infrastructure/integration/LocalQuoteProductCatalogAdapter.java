package com.cashly.cashly_api.quotes.infrastructure.integration;

import com.cashly.cashly_api.products.application.api.SalesProductCatalogQuery;
import com.cashly.cashly_api.quotes.application.contracts.QuoteProductCatalogGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class LocalQuoteProductCatalogAdapter implements QuoteProductCatalogGateway {
    private final SalesProductCatalogQuery products;

    public LocalQuoteProductCatalogAdapter(SalesProductCatalogQuery products) {
        this.products = products;
    }

    @Override
    public java.util.Optional<ProductReference> findProduct(UUID productId) {
        return products.findProduct(productId).map(product -> new ProductReference(product.id(), product.code(),
                product.name(), product.categoryId()));
    }

    @Override
    public Set<UUID> findProductIdsByCurrentCategory(UUID categoryId) {
        return products.findProductIdsByCurrentCategory(categoryId);
    }

    @Override
    public PageResponse<ProductEntry> findForCustomer(UUID customerId, String search, UUID categoryId,
                                                       Boolean composite, int page, int size) {
        var result = products.findForTransaction(customerId, search, categoryId, composite, null, page, size);
        var content = result.content().stream().map(product -> new ProductEntry(product.id(), product.code(),
                product.name(), product.categoryId(), product.categoryName(), product.composite(), product.active(),
                product.photo(), product.customerPrice())).toList();
        return new PageResponse<>(content, result.totalElements(), result.totalPages(), result.size(),
                result.number(), result.first(), result.last(), result.empty());
    }
}
