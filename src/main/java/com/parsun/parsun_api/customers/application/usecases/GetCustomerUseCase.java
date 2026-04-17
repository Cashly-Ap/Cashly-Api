package com.parsun.parsun_api.customers.application.usecases;

import com.parsun.parsun_api.customers.application.dto.response.CustomerDetailResponse;
import com.parsun.parsun_api.customers.application.exceptions.CustomerNotFoundException;
import com.parsun.parsun_api.customers.application.mappers.CustomerMapper;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import org.springframework.transaction.annotation.Transactional;

public class GetCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public GetCustomerUseCase(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional(readOnly = true)
    public CustomerDetailResponse execute(String id) {
        CustomerId customerId = CustomerId.from(id);
        return customerRepository.findById(customerId)
            .map(customerMapper::toDetailResponse)
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
    }
}
