package com.parsun.parsun_api.customers.application.usecases;

import com.parsun.parsun_api.customers.application.dto.response.CustomerListResponse;
import com.parsun.parsun_api.customers.application.mappers.CustomerMapper;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class ListCustomersUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public ListCustomersUseCase(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional(readOnly = true)
    public List<CustomerListResponse> execute() {
        return customerRepository.findAll().stream()
            .map(customerMapper::toListResponse)
            .toList();
    }
}
