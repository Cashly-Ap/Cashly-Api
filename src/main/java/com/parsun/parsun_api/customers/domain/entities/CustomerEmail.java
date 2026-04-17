package com.parsun.parsun_api.customers.domain.entities;

import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.customers.domain.valueobjects.EmailAddress;
import com.parsun.parsun_api.customers.domain.valueobjects.EmailId;

import java.util.Objects;

public class CustomerEmail {
    private final EmailId id;
    private final CustomerId customerId;
    private EmailAddress email;

    public CustomerEmail(EmailId id, CustomerId customerId, EmailAddress email) {
        if (id == null) throw new IllegalArgumentException("Email ID cannot be null");
        if (customerId == null) throw new IllegalArgumentException("Customer ID cannot be null");
        if (email == null) throw new IllegalArgumentException("Email address cannot be null");

        this.id = id;
        this.customerId = customerId;
        this.email = email;
    }

    public static CustomerEmail create(CustomerId customerId, EmailAddress email) {
        return new CustomerEmail(EmailId.generate(), customerId, email);
    }

    public EmailId getId() { return id; }
    public CustomerId getCustomerId() { return customerId; }
    public EmailAddress getEmail() { return email; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CustomerEmail that = (CustomerEmail) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
