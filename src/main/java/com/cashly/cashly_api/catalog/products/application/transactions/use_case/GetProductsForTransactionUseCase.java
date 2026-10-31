package com.cashly.cashly_api.catalog.products.application.transactions.use_case;

import com.cashly.cashly_api.catalog.products.application.ports.out.ProductCategoryGateway;
import com.cashly.cashly_api.catalog.products.application.transactions.response.ProductWithStockResponse;
import com.cashly.cashly_api.catalog.products.application.transactions.response.ProductWithStockResponse.WarehouseStockItem;
import com.cashly.cashly_api.catalog.products.domain.entity.Product;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.cashly.cashly_api.catalog.products.application.ports.out.ProductWarehouseStockGateway;
import com.cashly.cashly_api.catalog.products.application.ports.out.CustomerProductContextGateway;
import java.util.stream.Collectors;

public class GetProductsForTransactionUseCase {

    private final CustomerProductContextGateway customers;
    private final ProductRepository productRepository;
    private final ProductCategoryGateway categoryRepository;
    private final ProductWarehouseStockGateway stocks;

    public GetProductsForTransactionUseCase(
            CustomerProductContextGateway customers,
            ProductRepository productRepository,
            ProductCategoryGateway categoryRepository,
            ProductWarehouseStockGateway stocks) {
        this.customers = customers;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.stocks = stocks;
    }
    @Transactional(readOnly = true)
    public PageResponse<ProductWithStockResponse> execute(UUID customerId,
            String search, UUID categoryId, Boolean composite,
            Boolean withStock, int page, int size) {
        return execute(List.of(customerId), search, categoryId, composite,
                withStock, page, size);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductWithStockResponse> execute(
            List<UUID> customerIds, String search, UUID categoryId,
            Boolean composite, Boolean withStock, int page, int size) {
        UUID priceTypeId = customers.requirePriceType(customerIds);
        Set<UUID> allowedWarehouseIds = customerIds.stream()
                .flatMap(customerId -> customers
                        .accessibleWarehouseIds(customerId).stream())
                .collect(Collectors.toSet());
        PageResponse<Product> productsPage = productRepository.findAllForOrder(
                search, categoryId, composite, withStock, allowedWarehouseIds,
                page, size);

        List<ProductWithStockResponse> content = productsPage.content().stream()
                .map(product -> toResponse(product, priceTypeId,
                        allowedWarehouseIds))
                .toList();

        return new PageResponse<>(content, productsPage.totalElements(),
                productsPage.totalPages(), productsPage.size(),
                productsPage.number(), productsPage.first(),
                productsPage.last(), productsPage.empty());
    }

    private ProductWithStockResponse toResponse(Product product,
            UUID priceTypeId, Set<UUID> allowedWarehouseIds) {
        String categoryName = categoryRepository
                .findById(product.getCategoryId()).map(c -> c.name())
                .orElse("");

        BigDecimal customerPrice = resolveCustomerPrice(product, priceTypeId);

        List<WarehouseStockItem> stockItems = stocks
                .findByProductId(product.getId()).stream()
                .filter(stock -> stock.warehouseExists()
                        && allowedWarehouseIds.contains(stock.warehouseId()))
                .map(stock -> new WarehouseStockItem(
                        stock.warehouseId().toString(), stock.warehouseCode(),
                        stock.warehouseName(), stock.quantity(),
                        stock.reserved(), stock.available()))
                .toList();
        int totalStock = stockItems.stream()
                .mapToInt(WarehouseStockItem::quantity).sum();

        return new ProductWithStockResponse(product.getId().toString(),
                product.getCode(), product.getName(),
                product.getCategoryId().toString(), categoryName,
                product.isComposite(), product.isActive(), product.getPhoto(),
                customerPrice, totalStock, stockItems);
    }

    private BigDecimal resolveCustomerPrice(Product product, UUID priceTypeId) {
        if (priceTypeId == null) {
            return product.getPurchasePrice();
        }
        return product.getProductPrices().stream()
                .filter(pp -> priceTypeId.equals(pp.getPriceTypeId()))
                .map(pp -> pp.getPrice()).findFirst()
                .orElse(product.getPurchasePrice());
    }
}
