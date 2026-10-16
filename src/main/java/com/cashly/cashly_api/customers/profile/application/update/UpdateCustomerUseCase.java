package com.cashly.cashly_api.customers.profile.application.update;

import com.cashly.cashly_api.customers.profile.application.update.UpdateCustomerRequest;
import com.cashly.cashly_api.customers.profile.application.dto.response.CustomerDetailResponse;
import com.cashly.cashly_api.customers.profile.application.exceptions.CustomerNotFoundException;
import com.cashly.cashly_api.customers.profile.application.exceptions.DuplicateCustomerRifException;
import com.cashly.cashly_api.customers.profile.application.mappers.CustomerMapper;
import com.cashly.cashly_api.customers.profile.domain.Customer;
import com.cashly.cashly_api.customers.profile.domain.CustomerRepository;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CreditDays;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CreditLimit;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CustomerAlias;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CustomerType;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.EmailAddress;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.LegalName;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.PhoneNumber;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.Rif;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class UpdateCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public UpdateCustomerUseCase(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional
    public CustomerDetailResponse execute(String id, UpdateCustomerRequest request) {
        CustomerId customerId = CustomerId.from(id);

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));

        Rif rif = new Rif(request.rif());
        if (customerRepository.existsByRifAndIdNot(rif, customerId)) {
            throw new DuplicateCustomerRifException(rif.getValue());
        }

        customer.updateLegalName(new LegalName(request.legalName()));
        customer.updateType(CustomerType.valueOf(request.type()));
        customer.updateRif(rif);
        customer.updateAlias(new CustomerAlias(request.alias()));
        customer.updateCreditDays(new CreditDays(request.creditDays()));
        customer.updateCreditLimit(new CreditLimit(request.creditLimit()));
        customer.updateNotes(request.notes());
        customer.updatePriceTypeId(request.priceTypeId());

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

        Customer saved = customerRepository.save(customer);
        return customerMapper.toDetailResponse(saved, !customerRepository.hasAssociatedRecords(customer.getId()));
    }
}
