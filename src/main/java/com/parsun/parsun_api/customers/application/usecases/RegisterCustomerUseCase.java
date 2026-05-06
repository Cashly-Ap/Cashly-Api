package com.parsun.parsun_api.customers.application.usecases;

import com.parsun.parsun_api.customers.application.dto.request.CreateCustomerRequest;
import com.parsun.parsun_api.customers.application.dto.response.CustomerCreateResponse;
import com.parsun.parsun_api.customers.application.mappers.CustomerMapper;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.services.CustomerCodeGenerator;
import com.parsun.parsun_api.customers.domain.valueobjects.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class RegisterCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerCodeGenerator customerCodeGenerator;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;

    public RegisterCustomerUseCase(CustomerRepository customerRepository,
                                    CustomerCodeGenerator customerCodeGenerator,
                                    CustomerMapper customerMapper,
                                    PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.customerCodeGenerator = customerCodeGenerator;
        this.customerMapper = customerMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CustomerCreateResponse execute(CreateCustomerRequest request) {
        CustomerCode code = customerCodeGenerator.generateNext();

        Customer customer = Customer.create(
            code,
            new LegalName(request.legalName()),
            CustomerType.valueOf(request.type()),
            new Rif(request.rif()),
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

        if (Boolean.TRUE.equals(request.portalAccess())) {
            String encoded = passwordEncoder.encode(UUID.randomUUID().toString());
            customer.enablePortalAccess(encoded);
        }

        Customer saved = customerRepository.save(customer);
        return customerMapper.toCreateResponse(saved);
    }
}
