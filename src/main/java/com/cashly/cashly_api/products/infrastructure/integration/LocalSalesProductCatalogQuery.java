package com.cashly.cashly_api.products.infrastructure.integration;

import com.cashly.cashly_api.products.application.ports.in.SalesProductCatalogEntry;
import com.cashly.cashly_api.products.application.ports.in.SalesProductCatalogQuery;
import com.cashly.cashly_api.products.application.transactions.use_case.GetProductsForTransactionUseCase;
import com.cashly.cashly_api.products.domain.repository.ProductRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class LocalSalesProductCatalogQuery implements SalesProductCatalogQuery {
    private final ProductRepository products;
    private final GetProductsForTransactionUseCase transactionProducts;

    public LocalSalesProductCatalogQuery(ProductRepository products,
                                         GetProductsForTransactionUseCase transactionProducts) {
        this.products = products;
        this.transactionProducts = transactionProducts;
    }

    @Override
    public java.util.Optional<ProductReference> findProduct(UUID productId) {
        return products.findById(productId).map(product -> new ProductReference(product.getId(),
                product.getCode(), product.getName(), product.getCategoryId()));
    }

    @Override
    public Set<UUID> findProductIdsByCurrentCategory(UUID categoryId) {
        return products.findIdsByCategoryId(categoryId);
    }

    @Override
    public PageResponse<SalesProductCatalogEntry> findForTransaction(UUID customerId, String search,
                                                                      UUID categoryId, Boolean composite,
                                                                      Boolean withStock, int page, int size) {
        PageResponse<com.cashly.cashly_api.products.application.transactions.response.ProductWithStockResponse> result =
                transactionProducts.execute(customerId, search, categoryId, composite, withStock, page, size);
        var content = result.content().stream().map(product -> new SalesProductCatalogEntry(
                UUID.fromString(product.id()), product.code(), product.name(), UUID.fromString(product.categoryId()),
                product.categoryName(), product.composite(), product.active(), product.photo(), product.customerPrice(),
                product.totalStock(), product.stock().stream().map(stock -> new SalesProductCatalogEntry.Stock(
                        UUID.fromString(stock.warehouseId()), stock.warehouseCode(), stock.warehouseName(),
                        stock.quantity(), stock.reserved(), stock.available())).toList())).toList();
        return new PageResponse<>(content, result.totalElements(), result.totalPages(), result.size(),
                result.number(), result.first(), result.last(), result.empty());
    }
}
