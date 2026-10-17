package com.cashly.cashly_api.inventory.warehouses.stock.application.queries.services;

import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.ProductStockQuery;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
import java.util.List;
import java.util.UUID;

public class RepositoryProductStockQuery implements ProductStockQuery {
    private final WarehouseStockRepository stocks;
    private final WarehouseRepository warehouses;

    public RepositoryProductStockQuery(WarehouseStockRepository stocks, WarehouseRepository warehouses) {
        this.stocks = stocks;
        this.warehouses = warehouses;
    }

    @Override
    public List<Stock> findByProductId(UUID productId) {
        return stocks.findByProductId(productId).stream().map(stock -> {
            var warehouse = warehouses.findById(stock.getWarehouseId());
            return new Stock(stock.getWarehouseId(), warehouse.map(value -> value.getCode()).orElse(null),
                    warehouse.map(value -> value.getName()).orElse("Unknown"), warehouse.isPresent(),
                    stock.getQuantity(), stock.getReserved(), stock.getAvailableQuantity(), stock.getLastMovementAt());
        }).toList();
    }
}
