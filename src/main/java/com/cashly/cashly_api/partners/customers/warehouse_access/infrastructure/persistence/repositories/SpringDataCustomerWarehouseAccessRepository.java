package com.cashly.cashly_api.partners.customers.warehouse_access.infrastructure.persistence.repositories;

import com.cashly.cashly_api.partners.customers.warehouse_access.infrastructure.persistence.entities.CustomerWarehouseAccessEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataCustomerWarehouseAccessRepository
        extends JpaRepository<CustomerWarehouseAccessEntity, UUID> {

    Optional<CustomerWarehouseAccessEntity> findByCustomerIdAndWarehouseId(UUID customerId, UUID warehouseId);

    List<CustomerWarehouseAccessEntity> findByCustomerId(UUID customerId);

    List<CustomerWarehouseAccessEntity> findByWarehouseId(UUID warehouseId);

    boolean existsByCustomerIdAndWarehouseId(UUID customerId, UUID warehouseId);
}
