package com.cashly.cashly_api.customers.application.usecases;

import com.cashly.cashly_api.customers.application.dto.request.CreateCustomerRequest;
import com.cashly.cashly_api.customers.application.dto.response.CustomerCreateResponse;
import com.cashly.cashly_api.customers.application.exceptions.DuplicateCustomerRifException;
import com.cashly.cashly_api.customers.application.mappers.CustomerMapper;
import com.cashly.cashly_api.customers.domain.entities.Customer;
import com.cashly.cashly_api.customers.domain.entities.CustomerWarehouseAccess;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.cashly.cashly_api.customers.domain.services.CustomerCodeGenerator;
import com.cashly.cashly_api.customers.domain.valueobjects.*;
import com.cashly.cashly_api.customers.application.contracts.WarehouseDirectoryGateway;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class RegisterCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerCodeGenerator customerCodeGenerator;
    private final CustomerMapper customerMapper;
    private final CustomerWarehouseAccessRepository warehouseAccessRepository;
    private final WarehouseDirectoryGateway warehouseDirectory;

    public RegisterCustomerUseCase(CustomerRepository customerRepository,
                                    CustomerCodeGenerator customerCodeGenerator,
                                    CustomerMapper customerMapper,
                                    CustomerWarehouseAccessRepository warehouseAccessRepository,
                                    WarehouseDirectoryGateway warehouseDirectory) {
        this.customerRepository = customerRepository;
        this.customerCodeGenerator = customerCodeGenerator;
        this.customerMapper = customerMapper;
        this.warehouseAccessRepository = warehouseAccessRepository;
        this.warehouseDirectory = warehouseDirectory;
    }

    @Transactional
    public CustomerCreateResponse execute(CreateCustomerRequest request, UUID grantedBy) {
        Rif rif = new Rif(request.rif());
        if (customerRepository.existsByRif(rif)) {
            throw new DuplicateCustomerRifException(rif.getValue());
        }

        CustomerCode code = customerCodeGenerator.generateNext();

        Customer customer = Customer.create(
            code,
            new LegalName(request.legalName()),
            CustomerType.valueOf(request.type()),
            rif,
            new CustomerAlias(request.alias()),
            new CreditDays(request.creditDays()),
            new CreditLimit(request.creditLimit())
        );

        if (!request.active()) {
            customer.deactivate();
        }

        customer.updateNotes(request.notes());
        customer.updatePriceTypeId(request.priceTypeId());

        request.emails().forEach(e -> customer.addEmail(new EmailAddress(e.email())));
        request.phones().forEach(p -> customer.addPhone(new PhoneNumber(p.phone())));
        request.addresses().forEach(a -> customer.addAddress(
            a.address(), a.billing(), a.active(),
            a.state(), a.municipality(), a.postalCode()
        ));

        Customer saved = customerRepository.save(customer);
        grantWarehouseAccess(saved.getId().getValue(), request.warehouseIds(), grantedBy);
        return customerMapper.toCreateResponse(saved);
    }

    private void grantWarehouseAccess(UUID customerId, java.util.List<UUID> warehouseIds, UUID grantedBy) {
        for (UUID warehouseId : warehouseIds) {
            warehouseDirectory.requireById(warehouseId);
            warehouseAccessRepository.save(CustomerWarehouseAccess.create(customerId, warehouseId, grantedBy));
        }
    }
}
