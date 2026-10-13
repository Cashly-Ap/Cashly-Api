package com.cashly.cashly_api.products.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.products.application.ports.out.ProductWarehouseStockGateway;
import com.cashly.cashly_api.warehouses.application.ports.in.ProductStockQuery;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Component
public class LocalProductWarehouseStockGatewayAdapter implements ProductWarehouseStockGateway {
    private final ProductStockQuery stocks;

    public LocalProductWarehouseStockGatewayAdapter(ProductStockQuery stocks) {
        this.stocks = stocks;
    }

    @Override
    public List<Stock> findByProductId(UUID productId) {
        return stocks.findByProductId(productId).stream().map(stock -> new Stock(stock.warehouseId(),
                stock.warehouseCode(), stock.warehouseName(), stock.warehouseExists(), stock.quantity(),
                stock.reserved(), stock.available(), stock.lastMovementAt())).toList();
    }
}
