package com.cashly.cashly_api.customers.profile.application.get;

import com.cashly.cashly_api.customers.profile.application.dto.response.CustomerDetailResponse;
import com.cashly.cashly_api.customers.profile.application.exceptions.CustomerNotFoundException;
import com.cashly.cashly_api.customers.profile.application.mappers.CustomerMapper;
import com.cashly.cashly_api.customers.profile.domain.CustomerRepository;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CustomerId;
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
            .map(c -> customerMapper.toDetailResponse(c, !customerRepository.hasAssociatedRecords(customerId)))
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
    }
}
