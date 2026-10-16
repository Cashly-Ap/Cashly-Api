package com.cashly.cashly_api.customers.warehouse_access.application.queries;

import com.cashly.cashly_api.customers.warehouse_access.application.ports.in.CustomerWarehouseAccessQuery;
import com.cashly.cashly_api.customers.warehouse_access.domain.CustomerWarehouseAccess;
import com.cashly.cashly_api.customers.warehouse_access.domain.CustomerWarehouseAccessRepository;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class RepositoryCustomerWarehouseAccessQuery implements CustomerWarehouseAccessQuery {
    private final CustomerWarehouseAccessRepository repository;

    public RepositoryCustomerWarehouseAccessQuery(CustomerWarehouseAccessRepository repository) {
        this.repository = repository;
    }

    @Override
    public Set<UUID> accessibleWarehouseIds(UUID customerId) {
        return repository.findByCustomerId(customerId).stream().map(CustomerWarehouseAccess::getWarehouseId)
                .collect(Collectors.toSet());
    }
}
