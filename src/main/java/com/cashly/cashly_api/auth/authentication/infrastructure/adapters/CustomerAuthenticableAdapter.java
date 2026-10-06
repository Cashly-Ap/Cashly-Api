package com.cashly.cashly_api.auth.authentication.infrastructure.adapters;

import com.cashly.cashly_api.auth.authentication.domain.entities.Authenticable;
import com.cashly.cashly_api.auth.authentication.domain.entities.AuthenticableType;
import com.cashly.cashly_api.customers.domain.entities.Customer;
import java.time.LocalDateTime;
import java.util.Set;

public class CustomerAuthenticableAdapter implements Authenticable {
    private final Customer customer;

    public CustomerAuthenticableAdapter(Customer customer) {
        this.customer = customer;
    }

    @Override
    public String getSubjectId() {
        return customer.getId().getValue().toString();
    }

    @Override
    public String getEmail() {
        if (customer.getEmails() != null && !customer.getEmails().isEmpty()) {
            return customer.getEmails().get(0).getEmail().getValue();
        }
        return "";
    }

    @Override
    public String getHashedPassword() {
        return customer.getPortalPassword();
    }

    @Override
    public String getUserName() {
        return customer.getLegalName().getValue();
    }

    @Override
    public AuthenticableType getType() {
        return AuthenticableType.CUSTOMER;
    }

    @Override
    public boolean canAuthenticate() {
        return customer.isActive() && customer.isPortalAccess();
    }

    @Override
    public boolean isActive() {
        return customer.isActive();
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return customer.getCreatedAt();
    }

    public Customer getCustomer() {
        return customer;
    }
}
