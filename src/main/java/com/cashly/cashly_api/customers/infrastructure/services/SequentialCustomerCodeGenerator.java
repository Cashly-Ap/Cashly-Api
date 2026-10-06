package com.cashly.cashly_api.customers.infrastructure.services;

import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.services.CustomerCodeGenerator;
import com.cashly.cashly_api.customers.domain.valueobjects.CustomerCode;

public class SequentialCustomerCodeGenerator implements CustomerCodeGenerator {

    private final CustomerRepository customerRepository;

    public SequentialCustomerCodeGenerator(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public CustomerCode generateNext() {
        long nextCode = customerRepository.getNextCode();
        return new CustomerCode(nextCode);
    }
}
