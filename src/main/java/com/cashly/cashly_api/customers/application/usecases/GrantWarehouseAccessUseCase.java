package com.cashly.cashly_api.customers.application.usecases;

import com.cashly.cashly_api.customers.application.dto.response.CustomerWarehouseAccessResponse;
import com.cashly.cashly_api.customers.application.exceptions.CustomerWarehouseAccessAlreadyExistsException;
import com.cashly.cashly_api.customers.application.exceptions.CustomerNotFoundException;
import com.cashly.cashly_api.customers.domain.entities.CustomerWarehouseAccess;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.cashly.cashly_api.customers.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.customers.application.contracts.WarehouseSnapshot;
import com.cashly.cashly_api.customers.application.contracts.WarehouseDirectoryGateway;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GrantWarehouseAccessUseCase {

    private final CustomerWarehouseAccessRepository accessRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseDirectoryGateway warehouseDirectory;

    public GrantWarehouseAccessUseCase(CustomerWarehouseAccessRepository accessRepository,
                                        CustomerRepository customerRepository,
                                        WarehouseDirectoryGateway warehouseDirectory) {
        this.accessRepository = accessRepository;
        this.customerRepository = customerRepository;
        this.warehouseDirectory = warehouseDirectory;
    }

    @Transactional
    public CustomerWarehouseAccessResponse execute(UUID customerId, UUID warehouseId, UUID grantedBy) {
        customerRepository.findById(new CustomerId(customerId))
                .orElseThrow(() -> new CustomerNotFoundException(customerId.toString()));

        WarehouseSnapshot warehouse = warehouseDirectory.requireById(warehouseId);

        if (accessRepository.existsByCustomerIdAndWarehouseId(customerId, warehouseId)) {
            throw new CustomerWarehouseAccessAlreadyExistsException(customerId, warehouseId);
        }

        CustomerWarehouseAccess access = CustomerWarehouseAccess.create(customerId, warehouseId, grantedBy);
        CustomerWarehouseAccess saved = accessRepository.save(access);

        return CustomerWarehouseAccessResponse.from(saved, warehouse.name());
    }
}
