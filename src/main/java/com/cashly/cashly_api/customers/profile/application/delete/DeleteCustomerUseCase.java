package com.cashly.cashly_api.customers.profile.application.delete;

import com.cashly.cashly_api.customers.profile.application.delete.CustomerHasAssociatedRecordsException;
import com.cashly.cashly_api.customers.profile.application.exceptions.CustomerNotFoundException;
import com.cashly.cashly_api.customers.profile.domain.CustomerRepository;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CustomerId;
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
        if (customerRepository.hasAssociatedRecords(customerId)) {
            throw new CustomerHasAssociatedRecordsException(id);
        }
        customerRepository.delete(customerId);
    }
}
