package com.cashly.cashly_api.products.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.products.application.ports.out.ProductStockFilterGateway;
import com.cashly.cashly_api.warehouses.application.ports.in.AvailableStockProductsQuery;
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