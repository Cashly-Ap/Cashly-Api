package com.cashly.cashly_api.platform.auth.user.application.exceptions;

public class UserHasAssociatedRecordsException extends RuntimeException {
    public UserHasAssociatedRecordsException(String id) {
        super("User " + id + " has associated records and cannot be deleted");
    }
}
