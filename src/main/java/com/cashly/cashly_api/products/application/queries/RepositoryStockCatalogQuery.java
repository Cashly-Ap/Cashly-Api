package com.cashly.cashly_api.products.application.queries;

import com.cashly.cashly_api.products.application.api.StockCatalogEntry;
import com.cashly.cashly_api.products.application.api.StockCatalogQuery;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
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
