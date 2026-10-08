package com.cashly.cashly_api.receivables.infrastructure.integration;

import com.cashly.cashly_api.customers.application.api.CustomerProfileQuery;
import com.cashly.cashly_api.customers.application.api.CustomerProfileSnapshot;
import com.cashly.cashly_api.customers.application.exceptions.CustomerNotFoundException;
import com.cashly.cashly_api.receivables.application.contracts.ReceivablesCustomerProfileGateway;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class LocalReceivablesCustomerProfileGatewayAdapter implements ReceivablesCustomerProfileGateway {
    private final CustomerProfileQuery customerProfiles;

    public LocalReceivablesCustomerProfileGatewayAdapter(CustomerProfileQuery customerProfiles) {
        this.customerProfiles = customerProfiles;
    }

    @Override
    public Optional<CustomerProfile> findById(UUID customerId) {
        try {
            CustomerProfileSnapshot snapshot = customerProfiles.find(customerId);
            return Optional.of(new CustomerProfile(snapshot.legalName(), snapshot.rif(), snapshot.creditLimit(),
                    snapshot.currentBalance(), snapshot.phones(), snapshot.emails()));
        } catch (CustomerNotFoundException exception) {
            return Optional.empty();
        }
    }
}
