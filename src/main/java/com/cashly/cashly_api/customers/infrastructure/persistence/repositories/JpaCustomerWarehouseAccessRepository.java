package com.cashly.cashly_api.customers.infrastructure.persistence.repositories;

import com.cashly.cashly_api.customers.domain.entities.CustomerWarehouseAccess;
import com.cashly.cashly_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.cashly.cashly_api.customers.infrastructure.persistence.entities.CustomerWarehouseAccessEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaCustomerWarehouseAccessRepository implements CustomerWarehouseAccessRepository {

    private final SpringDataCustomerWarehouseAccessRepository springDataRepository;

    public JpaCustomerWarehouseAccessRepository(
            SpringDataCustomerWarehouseAccessRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public CustomerWarehouseAccess save(CustomerWarehouseAccess access) {
        CustomerWarehouseAccessEntity entity = CustomerWarehouseAccessEntity.fromDomain(access);
        return springDataRepository.save(entity).toDomain();
    }

    @Override
    public Optional<CustomerWarehouseAccess> findByCustomerIdAndWarehouseId(UUID customerId, UUID warehouseId) {
        return springDataRepository.findByCustomerIdAndWarehouseId(customerId, warehouseId)
                .map(CustomerWarehouseAccessEntity::toDomain);
    }

    @Override
    public List<CustomerWarehouseAccess> findByCustomerId(UUID customerId) {
        return springDataRepository.findByCustomerId(customerId).stream()
                .map(CustomerWarehouseAccessEntity::toDomain)
                .toList();
    }

    @Override
    public List<CustomerWarehouseAccess> findByWarehouseId(UUID warehouseId) {
        return springDataRepository.findByWarehouseId(warehouseId).stream()
                .map(CustomerWarehouseAccessEntity::toDomain)
                .toList();
    }

    @Override
    public void delete(CustomerWarehouseAccess access) {
        springDataRepository.deleteById(access.getId());
    }

    @Override
    public boolean existsByCustomerIdAndWarehouseId(UUID customerId, UUID warehouseId) {
        return springDataRepository.existsByCustomerIdAndWarehouseId(customerId, warehouseId);
    }
}
