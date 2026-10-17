package com.cashly.cashly_api.partners.customers.profile.application.exceptions;

public class DuplicateCustomerRifException extends RuntimeException {

    public DuplicateCustomerRifException(String rif) {
        super("A customer with RIF " + rif + " is already registered");
    }
}
