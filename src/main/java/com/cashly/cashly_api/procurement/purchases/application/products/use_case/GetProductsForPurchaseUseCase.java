package com.cashly.cashly_api.procurement.purchases.application.products.use_case;

import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseWarehouseGateway;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseWarehouseGateway.Warehouse;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseWarehouseGateway.Stock;

import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseProductCatalogGateway;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseProductCatalogGateway.Entry;

import com.cashly.cashly_api.procurement.purchases.application.dto.response.PurchaseProductResponse;
import com.cashly.cashly_api.procurement.purchases.application.dto.response.PurchaseProductResponse.WarehouseStockItem;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetProductsForPurchaseUseCase {

    private final PurchaseProductCatalogGateway productRepository;

    private final PurchaseWarehouseGateway warehouseRepository;

    public GetProductsForPurchaseUseCase(
            PurchaseProductCatalogGateway productRepository,

            PurchaseWarehouseGateway warehouseRepository) {
        this.productRepository = productRepository;

        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<PurchaseProductResponse> execute(String search,
            UUID categoryId, int page, int size) {
        List<Warehouse> allWarehouses = warehouseRepository.activeWarehouses();

        PageResponse<Entry> productsPage = productRepository.find(search,
                categoryId, page, size);

        List<PurchaseProductResponse> content = productsPage.content().stream()
                .map(product -> toResponse(product, allWarehouses)).toList();

        return new PageResponse<>(content, productsPage.totalElements(),
                productsPage.totalPages(), productsPage.size(),
                productsPage.number(), productsPage.first(),
                productsPage.last(), productsPage.empty());
    }

    private PurchaseProductResponse toResponse(Entry product,
            List<Warehouse> allWarehouses) {
        String categoryName = product.categoryName();

        Map<UUID, Stock> stockByWarehouse = warehouseRepository
                .findByProductId(product.id()).stream()
                .collect(Collectors.toMap(Stock::warehouseId, s -> s));

        List<WarehouseStockItem> stockItems = allWarehouses.stream()
                .map(warehouse -> {
                    Stock stock = stockByWarehouse.get(warehouse.id());
                    java.math.BigDecimal quantity = stock != null
                            ? stock.quantity()
                            : java.math.BigDecimal.ZERO;
                    java.math.BigDecimal reserved = stock != null
                            ? stock.reserved()
                            : java.math.BigDecimal.ZERO;
                    java.math.BigDecimal available = stock != null
                            ? stock.available()
                            : java.math.BigDecimal.ZERO;
                    return new WarehouseStockItem(warehouse.id().toString(),
                            warehouse.code(), warehouse.name(), quantity,
                            reserved, available);
                }).toList();

        java.math.BigDecimal totalStock = stockItems.stream()
                .map(WarehouseStockItem::quantity)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return new PurchaseProductResponse(product.id().toString(),
                product.code(), product.name(), product.categoryId().toString(),
                categoryName, product.active(), totalStock, stockItems,
                product.purchasePrice());
    }
}
