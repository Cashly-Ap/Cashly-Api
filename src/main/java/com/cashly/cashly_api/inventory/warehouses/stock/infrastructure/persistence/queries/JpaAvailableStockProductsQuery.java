package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.queries;

import com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.repositories.SpringDataAvailableStockProductsRepository;

import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.AvailableStockProductsQuery;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.UUID;

@Component
public class JpaAvailableStockProductsQuery implements AvailableStockProductsQuery {
    private final SpringDataAvailableStockProductsRepository stocks;
    public JpaAvailableStockProductsQuery(SpringDataAvailableStockProductsRepository stocks) {
        this.stocks = stocks;
    }
    @Override
    public Set<UUID> availableProductIds(Set<UUID> warehouseIds) {
        return warehouseIds.isEmpty() ? Set.of() : Set.copyOf(stocks.findAvailableProductIds(warehouseIds));
    }
}