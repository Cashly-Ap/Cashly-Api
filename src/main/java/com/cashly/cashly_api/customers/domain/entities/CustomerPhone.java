package com.cashly.cashly_api.customers.domain.entities;

import com.cashly.cashly_api.customers.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.customers.domain.valueobjects.PhoneId;
import com.cashly.cashly_api.customers.domain.valueobjects.PhoneNumber;

import java.util.Objects;

public class CustomerPhone {
    private final PhoneId id;
    private final CustomerId customerId;
    private PhoneNumber phone;

    public CustomerPhone(PhoneId id, CustomerId customerId, PhoneNumber phone) {
        if (id == null) throw new IllegalArgumentException("Phone ID cannot be null");
        if (customerId == null) throw new IllegalArgumentException("Customer ID cannot be null");
        if (phone == null) throw new IllegalArgumentException("Phone number cannot be null");

        this.id = id;
        this.customerId = customerId;
        this.phone = phone;
    }

    public static CustomerPhone create(CustomerId customerId, PhoneNumber phone) {
        return new CustomerPhone(PhoneId.generate(), customerId, phone);
    }

    public PhoneId getId() { return id; }
    public CustomerId getCustomerId() { return customerId; }
    public PhoneNumber getPhone() { return phone; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CustomerPhone that = (CustomerPhone) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
