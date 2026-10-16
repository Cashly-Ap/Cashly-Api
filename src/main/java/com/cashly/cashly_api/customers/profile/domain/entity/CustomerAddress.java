package com.cashly.cashly_api.customers.profile.domain.entity;

import com.cashly.cashly_api.customers.profile.domain.valueobjects.AddressId;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CustomerId;

import java.util.Objects;

public class CustomerAddress {
    private final AddressId id;
    private final CustomerId customerId;
    private String address;
    private boolean billing;
    private boolean active;
    private String state;
    private String municipality;
    private String postalCode;

    public CustomerAddress(AddressId id, CustomerId customerId, String address, boolean billing, boolean active,
                           String state, String municipality, String postalCode) {
        if (id == null) throw new IllegalArgumentException("Address ID cannot be null");
        if (customerId == null) throw new IllegalArgumentException("Customer ID cannot be null");
        if (address == null || address.isBlank()) throw new IllegalArgumentException("Address cannot be null or blank");

        this.id = id;
        this.customerId = customerId;
        this.address = address.trim();
        this.billing = billing;
        this.active = active;
        this.state = state;
        this.municipality = municipality;
        this.postalCode = postalCode;
    }

    public static CustomerAddress create(CustomerId customerId, String address, boolean billing, boolean active,
                                         String state, String municipality, String postalCode) {
        return new CustomerAddress(AddressId.generate(), customerId, address, billing, active, state, municipality, postalCode);
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
    public String getState() { return state; }
    public String getMunicipality() { return municipality; }
    public String getPostalCode() { return postalCode; }

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
