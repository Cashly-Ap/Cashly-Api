package com.cashly.cashly_api.orders.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.customers.application.ports.in.CustomerWarehouseAccessQuery;
import com.cashly.cashly_api.orders.application.ports.out.OrderWarehouseGateway;
import com.cashly.cashly_api.warehouses.application.ports.in.WarehouseDirectoryQuery;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class LocalOrderWarehouseAdapter implements OrderWarehouseGateway {
    private final WarehouseDirectoryQuery warehouses;
    private final CustomerWarehouseAccessQuery customerAccess;

    public LocalOrderWarehouseAdapter(WarehouseDirectoryQuery warehouses,
                                      CustomerWarehouseAccessQuery customerAccess) {
        this.warehouses = warehouses;
        this.customerAccess = customerAccess;
    }

    @Override
    public Optional<Warehouse> findById(UUID warehouseId) {
        return warehouses.findById(warehouseId).map(warehouse -> new Warehouse(warehouse.id(), warehouse.name()));
    }

    @Override
    public boolean hasCustomerAccess(UUID customerId, UUID warehouseId) {
        return customerAccess.accessibleWarehouseIds(customerId).contains(warehouseId);
    }
}
