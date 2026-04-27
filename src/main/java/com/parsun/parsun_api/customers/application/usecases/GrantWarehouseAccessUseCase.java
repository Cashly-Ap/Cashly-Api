package com.parsun.parsun_api.customers.application.usecases;

import com.parsun.parsun_api.customers.application.dto.response.CustomerWarehouseAccessResponse;
import com.parsun.parsun_api.customers.application.exceptions.CustomerWarehouseAccessAlreadyExistsException;
import com.parsun.parsun_api.customers.application.exceptions.CustomerNotFoundException;
import com.parsun.parsun_api.customers.domain.entities.CustomerWarehouseAccess;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.warehouses.application.exceptions.WarehouseNotFoundException;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GrantWarehouseAccessUseCase {

    private final CustomerWarehouseAccessRepository accessRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;

    public GrantWarehouseAccessUseCase(CustomerWarehouseAccessRepository accessRepository,
                                        CustomerRepository customerRepository,
                                        WarehouseRepository warehouseRepository) {
        this.accessRepository = accessRepository;
        this.customerRepository = customerRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    public CustomerWarehouseAccessResponse execute(UUID customerId, UUID warehouseId, UUID grantedBy) {
        customerRepository.findById(new CustomerId(customerId))
                .orElseThrow(() -> new CustomerNotFoundException(customerId.toString()));

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));

        if (accessRepository.existsByCustomerIdAndWarehouseId(customerId, warehouseId)) {
            throw new CustomerWarehouseAccessAlreadyExistsException(customerId, warehouseId);
        }

        CustomerWarehouseAccess access = CustomerWarehouseAccess.create(customerId, warehouseId, grantedBy);
        CustomerWarehouseAccess saved = accessRepository.save(access);

        return CustomerWarehouseAccessResponse.from(saved, warehouse.getName(), warehouse.getCode());
    }
}
