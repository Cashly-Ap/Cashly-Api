package com.cashly.cashly_api.quotes.infrastructure.integration;

import com.cashly.cashly_api.customers.warehouse_access.application.ports.in.CustomerWarehouseAccessQuery;
import com.cashly.cashly_api.quotes.application.ports.out.QuoteWarehouseGateway;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.ProductStockQuery;
import com.cashly.cashly_api.inventory.warehouses.management.application.ports.in.WarehouseDirectoryQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class LocalQuoteWarehouseAdapter implements QuoteWarehouseGateway {
    private final WarehouseDirectoryQuery directory;
    private final CustomerWarehouseAccessQuery customerAccess;
    private final ProductStockQuery stocks;

    public LocalQuoteWarehouseAdapter(WarehouseDirectoryQuery directory,
                                      CustomerWarehouseAccessQuery customerAccess,
                                      ProductStockQuery stocks) {
        this.directory = directory;
        this.customerAccess = customerAccess;
        this.stocks = stocks;
    }

    @Override
    public Optional<Warehouse> findById(UUID warehouseId) {
        return directory.findById(warehouseId).map(warehouse -> new Warehouse(warehouse.id(),
                warehouse.name(), warehouse.address()));
    }

    @Override
    public java.util.Set<UUID> accessibleWarehouseIds(UUID customerId) {
        return customerAccess.accessibleWarehouseIds(customerId);
    }

    @Override
    public List<Stock> findStockByProductId(UUID productId) {
        return stocks.findByProductId(productId).stream().filter(stock -> stock.warehouseExists())
                .map(stock -> new Stock(stock.warehouseId(), stock.warehouseName(), stock.available())).toList();
    }
}
