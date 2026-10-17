package com.cashly.cashly_api.partners.customers.warehouse_access.domain.repository;

import com.cashly.cashly_api.partners.customers.warehouse_access.domain.entity.CustomerWarehouseAccess;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerWarehouseAccessRepository {

    CustomerWarehouseAccess save(CustomerWarehouseAccess access);

    Optional<CustomerWarehouseAccess> findByCustomerIdAndWarehouseId(UUID customerId, UUID warehouseId);

    List<CustomerWarehouseAccess> findByCustomerId(UUID customerId);

    List<CustomerWarehouseAccess> findByWarehouseId(UUID warehouseId);

    void delete(CustomerWarehouseAccess access);

    boolean existsByCustomerIdAndWarehouseId(UUID customerId, UUID warehouseId);
}
