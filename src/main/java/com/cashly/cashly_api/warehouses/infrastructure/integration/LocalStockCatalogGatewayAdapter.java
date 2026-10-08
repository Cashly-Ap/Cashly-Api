package com.cashly.cashly_api.warehouses.infrastructure.integration;

import com.cashly.cashly_api.products.application.api.StockCatalogQuery;
import com.cashly.cashly_api.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.warehouses.application.contracts.StockCatalogGateway;
import org.springframework.stereotype.Component;
import java.util.UUID;
import java.util.Optional;

@Component
public class LocalStockCatalogGatewayAdapter implements StockCatalogGateway {
    private final StockCatalogQuery catalog;

    public LocalStockCatalogGatewayAdapter(StockCatalogQuery catalog) {
        this.catalog = catalog;
    }

    @Override
    public CatalogProduct getProduct(UUID productId) {
        return findProduct(productId).orElseThrow(() -> new ProductNotFoundException(productId));
    }

    @Override
    public Optional<CatalogProduct> findProduct(UUID productId) {
        return catalog.findProduct(productId).map(product -> toCatalogProduct(product));
    }

    private CatalogProduct toCatalogProduct(com.cashly.cashly_api.products.application.api.StockCatalogEntry product) {
        return new CatalogProduct(product.id(), product.code(), product.name(), product.composite(),
                product.components().stream().map(component -> new Component(component.productId(), component.quantity())).toList());
    }
}
