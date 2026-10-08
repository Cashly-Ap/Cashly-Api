package com.cashly.cashly_api.customers.application.usecases;

import com.cashly.cashly_api.customers.application.dto.response.CustomerWarehouseAccessResponse;
import com.cashly.cashly_api.customers.application.exceptions.CustomerNotFoundException;
import com.cashly.cashly_api.customers.domain.entities.CustomerWarehouseAccess;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.cashly.cashly_api.customers.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.customers.application.contracts.WarehouseSnapshot;
import com.cashly.cashly_api.customers.application.contracts.WarehouseDirectoryGateway;

import java.util.List;
import java.util.UUID;

public class ListCustomerWarehousesUseCase {

    private final CustomerWarehouseAccessRepository accessRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseDirectoryGateway warehouseDirectory;

    public ListCustomerWarehousesUseCase(CustomerWarehouseAccessRepository accessRepository,
                                          CustomerRepository customerRepository,
                                          WarehouseDirectoryGateway warehouseDirectory) {
        this.accessRepository = accessRepository;
        this.customerRepository = customerRepository;
        this.warehouseDirectory = warehouseDirectory;
    }

    public List<CustomerWarehouseAccessResponse> execute(UUID customerId) {
        customerRepository.findById(new CustomerId(customerId))
                .orElseThrow(() -> new CustomerNotFoundException(customerId.toString()));

        List<CustomerWarehouseAccess> accesses = accessRepository.findByCustomerId(customerId);

        return accesses.stream()
                .map(access -> {
                    WarehouseSnapshot warehouse = warehouseDirectory.findById(access.getWarehouseId())
                            .orElse(null);
                    String name = warehouse != null ? warehouse.name() : "Unknown";
                    return CustomerWarehouseAccessResponse.from(access, name);
                })
                .toList();
    }
}
