package com.parsun.parsun_api.customers.application.usecases;

import com.parsun.parsun_api.customers.application.dto.request.ChangeCustomerPortalPasswordRequest;
import com.parsun.parsun_api.customers.application.exceptions.CustomerNotFoundException;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

public class ChangeCustomerPortalPasswordUseCase {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public ChangeCustomerPortalPasswordUseCase(CustomerRepository customerRepository,
                                                PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void execute(String id, ChangeCustomerPortalPasswordRequest request) {
        CustomerId customerId = CustomerId.from(id);

        var customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));

        String encoded = passwordEncoder.encode(request.newPassword());
        customer.changePortalPassword(encoded);

        customerRepository.save(customer);
    }
}
