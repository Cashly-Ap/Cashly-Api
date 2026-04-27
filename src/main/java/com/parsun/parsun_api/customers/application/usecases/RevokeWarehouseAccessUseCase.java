package com.parsun.parsun_api.customers.application.usecases;

import com.parsun.parsun_api.customers.application.exceptions.CustomerWarehouseAccessNotFoundException;
import com.parsun.parsun_api.customers.domain.entities.CustomerWarehouseAccess;
import com.parsun.parsun_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class RevokeWarehouseAccessUseCase {

    private final CustomerWarehouseAccessRepository accessRepository;

    public RevokeWarehouseAccessUseCase(CustomerWarehouseAccessRepository accessRepository) {
        this.accessRepository = accessRepository;
    }

    @Transactional
    public void execute(UUID customerId, UUID warehouseId) {
        CustomerWarehouseAccess access = accessRepository
                .findByCustomerIdAndWarehouseId(customerId, warehouseId)
                .orElseThrow(() -> new CustomerWarehouseAccessNotFoundException(customerId, warehouseId));

        accessRepository.delete(access);
    }
}
