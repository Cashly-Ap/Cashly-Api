package com.cashly.cashly_api.catalog.products.application.queries.services;

import com.cashly.cashly_api.catalog.products.application.ports.in.StockCatalogEntry;
import com.cashly.cashly_api.catalog.products.application.ports.in.StockCatalogQuery;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import java.util.Optional;
import java.util.UUID;

public class RepositoryStockCatalogQuery implements StockCatalogQuery {
    private final ProductRepository products;

    public RepositoryStockCatalogQuery(ProductRepository products) {
        this.products = products;
    }

    @Override
    public Optional<StockCatalogEntry> findProduct(UUID productId) {
        return products.findById(productId).map(product -> new StockCatalogEntry(product.getId(), product.getCode(),
                product.getName(), product.isComposite(), product.getComponents().stream()
                .map(component -> new StockCatalogEntry.Component(component.getComponentProductId(), component.getQuantity()))
                .toList()));
    }
}
