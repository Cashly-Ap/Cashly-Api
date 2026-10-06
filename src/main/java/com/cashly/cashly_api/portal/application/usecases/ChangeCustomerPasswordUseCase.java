package com.cashly.cashly_api.portal.application.usecases;

import com.cashly.cashly_api.auth.authentication.application.exceptions.InvalidCredentialsException;
import com.cashly.cashly_api.auth.user.domain.services.PasswordEncoder;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Password;
import com.cashly.cashly_api.customers.domain.entities.Customer;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.portal.application.dto.request.ChangeCustomerPasswordRequest;
import com.cashly.cashly_api.portal.application.dto.response.CustomerSummaryResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class ChangeCustomerPasswordUseCase {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public ChangeCustomerPasswordUseCase(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CustomerSummaryResponse execute(String customerId, ChangeCustomerPasswordRequest request) {
        CustomerId id = CustomerId.from(customerId);

        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new InvalidCredentialsException("Customer not found"));

        if (!passwordEncoder.matches(request.currentPassword(), customer.getPortalPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        if (!request.newPassword().equals(customer.getRif().getValue())) {
            Password.validateRawPassword(request.newPassword());
        }
        String encoded = passwordEncoder.encode(request.newPassword());
        customer.changePortalPassword(encoded);
        customerRepository.save(customer);

        return new CustomerSummaryResponse(
            UUID.fromString(customerId),
            customer.getLegalName().getValue(),
            customer.getRif().getValue(),
            null,
            null,
            null
        );
    }
}
