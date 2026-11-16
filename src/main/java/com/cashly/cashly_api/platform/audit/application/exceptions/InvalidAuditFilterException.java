package com.cashly.cashly_api.platform.audit.application.exceptions;

import com.cashly.cashly_api.platform.audit.domain.exceptions.AuditFilterException;

public class InvalidAuditFilterException extends AuditFilterException {

    public InvalidAuditFilterException(String messageKey,
            Object... messageArguments) {
        super(messageKey, messageArguments);
    }
}
