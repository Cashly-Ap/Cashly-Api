package com.parsun.parsun_api.customers.application.usecases;

import com.parsun.parsun_api.customers.application.dto.request.UpdateCustomerRequest;
import com.parsun.parsun_api.customers.application.dto.response.CustomerDetailResponse;
import com.parsun.parsun_api.customers.application.exceptions.CustomerNotFoundException;
import com.parsun.parsun_api.customers.application.mappers.CustomerMapper;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

public class UpdateCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;

    public UpdateCustomerUseCase(CustomerRepository customerRepository,
                                  CustomerMapper customerMapper,
                                  PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CustomerDetailResponse execute(String id, UpdateCustomerRequest request) {
        CustomerId customerId = CustomerId.from(id);

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));

        customer.updateLegalName(new LegalName(request.legalName()));
        customer.updateType(CustomerType.valueOf(request.type()));
        customer.updateRif(new Rif(request.rif()));
        customer.updateAlias(new CustomerAlias(request.alias()));
        customer.updateCreditDays(new CreditDays(request.creditDays()));
        customer.updateCreditLimit(new CreditLimit(request.creditLimit()));

        if (request.active()) {
            customer.activate();
        } else {
            customer.deactivate();
        }

        customer.clearEmails();
        customer.clearPhones();
        customer.clearAddresses();

        request.emails().forEach(e -> customer.addEmail(new EmailAddress(e.email())));
        request.phones().forEach(p -> customer.addPhone(new PhoneNumber(p.phone())));
        request.addresses().forEach(a -> customer.addAddress(
            a.address(), a.billing(), a.active(),
            a.state(), a.municipality(), a.postalCode()
        ));

        if (Boolean.TRUE.equals(request.portalAccess()) && request.portalEmail() != null) {
            String encoded = passwordEncoder.encode(request.portalEmail());
            customer.enablePortalAccess(request.portalEmail(), encoded);
        } else if (Boolean.FALSE.equals(request.portalAccess())) {
            customer.disablePortalAccess();
        }

        Customer saved = customerRepository.save(customer);
        return customerMapper.toDetailResponse(saved);
    }
}
