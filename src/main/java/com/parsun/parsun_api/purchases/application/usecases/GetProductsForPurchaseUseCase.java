package com.parsun.parsun_api.purchases.application.usecases;

import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.purchases.application.dto.response.PurchaseProductResponse;
import com.parsun.parsun_api.purchases.application.dto.response.PurchaseProductResponse.WarehouseStockItem;
import com.parsun.parsun_api.shared.dto.PageResponse;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetProductsForPurchaseUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final WarehouseRepository warehouseRepository;

    public GetProductsForPurchaseUseCase(ProductRepository productRepository,
                                          CategoryRepository categoryRepository,
                                          WarehouseStockRepository warehouseStockRepository,
                                          WarehouseRepository warehouseRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<PurchaseProductResponse> execute(String search, UUID categoryId,
                                                          int page, int size) {
        List<Warehouse> allWarehouses = warehouseRepository.findAllWithFilters(
                null, true, 0, 1000, "name", "asc").content();

        PageResponse<Product> productsPage = productRepository.findAllWithFilters(
                search, categoryId, true, false, page, size, "name", "asc");

        List<PurchaseProductResponse> content = productsPage.content().stream()
                .map(product -> toResponse(product, allWarehouses))
                .toList();

        return new PageResponse<>(content, productsPage.totalElements(), productsPage.totalPages(),
                productsPage.size(), productsPage.number(), productsPage.first(),
                productsPage.last(), productsPage.empty());
    }

    private PurchaseProductResponse toResponse(Product product, List<Warehouse> allWarehouses) {
        String categoryName = categoryRepository.findById(product.getCategoryId())
                .map(c -> c.getName())
                .orElse("");

        Map<UUID, WarehouseStock> stockByWarehouse = warehouseStockRepository
                .findByProductId(product.getId()).stream()
                .collect(Collectors.toMap(WarehouseStock::getWarehouseId, s -> s));

        List<WarehouseStockItem> stockItems = allWarehouses.stream()
                .map(warehouse -> {
                    WarehouseStock stock = stockByWarehouse.get(warehouse.getId());
                    int quantity = stock != null ? stock.getQuantity() : 0;
                    int reserved = stock != null ? stock.getReserved() : 0;
                    int available = stock != null ? stock.getAvailableQuantity() : 0;
                    return new WarehouseStockItem(
                            warehouse.getId().toString(),
                            warehouse.getCode(),
                            warehouse.getName(),
                            quantity,
                            reserved,
                            available
                    );
                })
                .toList();

        int totalStock = stockItems.stream().mapToInt(WarehouseStockItem::quantity).sum();

        return new PurchaseProductResponse(
                product.getId().toString(),
                product.getCode(),
                product.getName(),
                product.getCategoryId().toString(),
                categoryName,
                product.isActive(),
                totalStock,
                stockItems
        );
    }
}
