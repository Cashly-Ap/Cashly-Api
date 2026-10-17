package com.cashly.cashly_api.catalog.products.infrastructure.integration;

import com.cashly.cashly_api.catalog.products.application.ports.out.ProductStockFilterGateway;
import com.cashly.cashly_api.warehouses.stock.application.ports.in.AvailableStockProductsQuery;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.UUID;

@Component
public class LocalProductStockFilterGatewayAdapter implements ProductStockFilterGateway {
    private final AvailableStockProductsQuery stocks;
    public LocalProductStockFilterGatewayAdapter(AvailableStockProductsQuery stocks) {
        this.stocks = stocks;
    }
    @Override
    public Set<UUID> availableProductIds(Set<UUID> warehouseIds) {
        return Set.copyOf(stocks.availableProductIds(warehouseIds));
    }
}