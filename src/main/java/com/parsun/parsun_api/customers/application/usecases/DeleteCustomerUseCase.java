package com.parsun.parsun_api.customers.application.usecases;

import com.parsun.parsun_api.customers.application.exceptions.CustomerNotFoundException;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import org.springframework.transaction.annotation.Transactional;

public class DeleteCustomerUseCase {

    private final CustomerRepository customerRepository;

    public DeleteCustomerUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public void execute(String id) {
        CustomerId customerId = CustomerId.from(id);
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException("Customer not found with id: " + id);
        }
        customerRepository.delete(customerId);
    }
}
