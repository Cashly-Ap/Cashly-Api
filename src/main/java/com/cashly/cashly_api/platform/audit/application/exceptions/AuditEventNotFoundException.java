package com.cashly.cashly_api.platform.audit.application.exceptions;

public class AuditEventNotFoundException extends RuntimeException {

    public AuditEventNotFoundException(String message) {
        super(message);
    }
}
