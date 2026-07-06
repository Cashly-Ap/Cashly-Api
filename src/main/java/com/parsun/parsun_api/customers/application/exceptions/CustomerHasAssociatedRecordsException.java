package com.parsun.parsun_api.customers.application.exceptions;

public class CustomerHasAssociatedRecordsException extends RuntimeException {
    public CustomerHasAssociatedRecordsException(String id) {
        super("Customer " + id + " has associated records and cannot be deleted");
    }
}
