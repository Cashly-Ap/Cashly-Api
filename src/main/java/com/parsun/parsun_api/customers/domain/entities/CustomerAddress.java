package com.parsun.parsun_api.customers.domain.entities;

import com.parsun.parsun_api.customers.domain.valueobjects.AddressId;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;

import java.util.Objects;

public class CustomerAddress {
    private final AddressId id;
    private final CustomerId customerId;
    private String address;
    private boolean billing;
    private boolean active;

    public CustomerAddress(AddressId id, CustomerId customerId, String address, boolean billing, boolean active) {
        if (id == null) throw new IllegalArgumentException("Address ID cannot be null");
        if (customerId == null) throw new IllegalArgumentException("Customer ID cannot be null");
        if (address == null || address.isBlank()) throw new IllegalArgumentException("Address cannot be null or blank");

        this.id = id;
        this.customerId = customerId;
        this.address = address.trim();
        this.billing = billing;
        this.active = active;
    }

    public static CustomerAddress create(CustomerId customerId, String address, boolean billing, boolean active) {
        return new CustomerAddress(AddressId.generate(), customerId, address, billing, active);
    }

    public void markAsBilling() {
        this.billing = true;
    }

    public void unmarkAsBilling() {
        this.billing = false;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public AddressId getId() { return id; }
    public CustomerId getCustomerId() { return customerId; }
    public String getAddress() { return address; }
    public boolean isBilling() { return billing; }
    public boolean isActive() { return active; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CustomerAddress that = (CustomerAddress) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
