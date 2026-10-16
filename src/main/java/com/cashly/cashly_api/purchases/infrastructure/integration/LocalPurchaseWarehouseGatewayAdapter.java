package com.cashly.cashly_api.purchases.infrastructure.integration;

import com.cashly.cashly_api.purchases.application.ports.out.PurchaseWarehouseGateway;
import com.cashly.cashly_api.warehouses.stock.application.ports.in.PurchaseWarehouseQuery;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class LocalPurchaseWarehouseGatewayAdapter implements PurchaseWarehouseGateway {
    private final PurchaseWarehouseQuery query;
    public LocalPurchaseWarehouseGatewayAdapter(PurchaseWarehouseQuery query) {
        this.query = query;
    }
    @Override
    public Optional<Warehouse> findById(UUID id) {
        return query.findById(id).map(warehouse -> new Warehouse(warehouse.id(), warehouse.code(), warehouse.name()));
    }
    @Override
    public List<Warehouse> activeWarehouses() {
        return query.activeWarehouses().stream().map(warehouse -> new Warehouse(warehouse.id(), warehouse.code(), warehouse.name())).toList();
    }
    @Override
    public List<Stock> findByProductId(UUID productId) {
        return query.findByProductId(productId).stream().map(stock -> new Stock(stock.warehouseId(), stock.quantity(), stock.reserved(), stock.available())).toList();
    }
}