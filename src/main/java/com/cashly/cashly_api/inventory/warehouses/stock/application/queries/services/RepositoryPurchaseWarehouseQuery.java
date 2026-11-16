package com.cashly.cashly_api.inventory.warehouses.stock.application.queries.services;

import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.PurchaseWarehouseQuery;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RepositoryPurchaseWarehouseQuery
        implements
            PurchaseWarehouseQuery {
    private static final int DIRECTORY_PAGE_SIZE = 1000;
    private final WarehouseRepository warehouses;
    private final WarehouseStockRepository stocks;
    public RepositoryPurchaseWarehouseQuery(WarehouseRepository warehouses,
            WarehouseStockRepository stocks) {
        this.warehouses = warehouses;
        this.stocks = stocks;
    }
    @Override
    public Optional<Warehouse> findById(UUID id) {
        return warehouses.findById(id)
                .map(warehouse -> new Warehouse(warehouse.getId(),
                        warehouse.getCode(), warehouse.getName()));
    }
    @Override
    public List<Warehouse> activeWarehouses() {
        return warehouses
                .findAllWithFilters(null, true, 0, DIRECTORY_PAGE_SIZE, "name",
                        "asc")
                .content().stream()
                .map(warehouse -> new Warehouse(warehouse.getId(),
                        warehouse.getCode(), warehouse.getName()))
                .toList();
    }
    @Override
    public List<Stock> findByProductId(UUID productId) {
        return stocks.findByProductId(productId).stream()
                .map(stock -> new Stock(stock.getWarehouseId(),
                        stock.getQuantity(), stock.getReserved(),
                        stock.getAvailableQuantity()))
                .toList();
    }
}