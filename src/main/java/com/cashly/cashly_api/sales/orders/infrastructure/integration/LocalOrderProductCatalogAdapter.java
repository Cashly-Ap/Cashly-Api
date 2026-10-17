package com.cashly.cashly_api.sales.orders.infrastructure.integration;

import com.cashly.cashly_api.sales.orders.application.ports.out.OrderProductCatalogGateway;
import com.cashly.cashly_api.catalog.products.application.ports.in.SalesProductCatalogQuery;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class LocalOrderProductCatalogAdapter implements OrderProductCatalogGateway {
    private final SalesProductCatalogQuery products;

    public LocalOrderProductCatalogAdapter(SalesProductCatalogQuery products) {
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
                                                       Boolean composite, Boolean withStock, int page, int size) {
        var result = products.findForTransaction(customerId, search, categoryId, composite, withStock, page, size);
        var content = result.content().stream().map(product -> new ProductEntry(product.id(), product.code(),
                product.name(), product.categoryId(), product.categoryName(), product.composite(), product.active(),
                product.photo(), product.customerPrice(), product.totalStock(), product.stock().stream()
                .map(stock -> new Stock(stock.warehouseId(), stock.warehouseCode(), stock.warehouseName(),
                        stock.quantity(), stock.reserved(), stock.available())).toList())).toList();
        return new PageResponse<>(content, result.totalElements(), result.totalPages(), result.size(),
                result.number(), result.first(), result.last(), result.empty());
    }
}
