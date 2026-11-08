package com.cashly.cashly_api.platform.audit.application.exceptions;

import com.cashly.cashly_api.platform.audit.domain.exceptions.AuditFilterException;

public class UnknownAuditEntityTypeException extends AuditFilterException {

    public UnknownAuditEntityTypeException(String value) {
        super("audit.errors.unknownEntityType", value);
    }
}
