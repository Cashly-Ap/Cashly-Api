package com.cashly.cashly_api.customers.warehouse_access.application.revoke;

import com.cashly.cashly_api.customers.warehouse_access.application.exceptions.CustomerWarehouseAccessNotFoundException;
import com.cashly.cashly_api.customers.warehouse_access.domain.CustomerWarehouseAccess;
import com.cashly.cashly_api.customers.warehouse_access.domain.CustomerWarehouseAccessRepository;
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
