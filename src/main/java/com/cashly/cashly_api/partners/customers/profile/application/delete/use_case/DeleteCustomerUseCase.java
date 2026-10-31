package com.cashly.cashly_api.partners.customers.profile.application.delete.use_case;

import com.cashly.cashly_api.partners.customers.profile.application.delete.services.CustomerHasAssociatedRecordsException;
import com.cashly.cashly_api.partners.customers.profile.application.exceptions.CustomerNotFoundException;
import com.cashly.cashly_api.partners.customers.profile.domain.repository.CustomerRepository;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CustomerId;
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
            throw new CustomerNotFoundException(
                    "Customer not found with id: " + id);
        }
        if (customerRepository.hasAssociatedRecords(customerId)) {
            throw new CustomerHasAssociatedRecordsException(id);
        }
        customerRepository.delete(customerId);
    }
}
