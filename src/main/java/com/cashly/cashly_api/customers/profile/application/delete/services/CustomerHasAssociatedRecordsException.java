package com.cashly.cashly_api.customers.profile.application.delete.services;

public class CustomerHasAssociatedRecordsException extends RuntimeException {
    public CustomerHasAssociatedRecordsException(String id) {
        super("Customer " + id + " has associated records and cannot be deleted");
    }
}
