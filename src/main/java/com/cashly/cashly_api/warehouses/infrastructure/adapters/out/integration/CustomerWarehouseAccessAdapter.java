package com.cashly.cashly_api.warehouses.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.customers.application.ports.in.CustomerWarehouseAccessQuery;
import com.cashly.cashly_api.warehouses.application.ports.out.CustomerWarehouseAccessGateway;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.UUID;

@Component
public class CustomerWarehouseAccessAdapter implements CustomerWarehouseAccessGateway {
    private final CustomerWarehouseAccessQuery query;

    public CustomerWarehouseAccessAdapter(CustomerWarehouseAccessQuery query) {
        this.query = query;
    }

    @Override
    public Set<UUID> accessibleWarehouseIds(UUID customerId) {
        return query.accessibleWarehouseIds(customerId);
    }
}
