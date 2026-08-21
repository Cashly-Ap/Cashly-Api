package com.parsun.parsun_api.customers.application.exceptions;

public class DuplicateCustomerRifException extends RuntimeException {

    public DuplicateCustomerRifException(String rif) {
        super("A customer with RIF " + rif + " is already registered");
    }
}
