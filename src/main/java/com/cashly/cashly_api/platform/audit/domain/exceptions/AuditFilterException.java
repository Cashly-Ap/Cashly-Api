package com.cashly.cashly_api.platform.audit.domain.exceptions;

import java.util.Arrays;

public class AuditFilterException extends RuntimeException {

    private final String messageKey;
    private final Object[] messageArguments;

    public AuditFilterException(String messageKey, Object... messageArguments) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageArguments = messageArguments == null
                ? new Object[0]
                : Arrays.copyOf(messageArguments, messageArguments.length);
    }

    public String messageKey() {
        return messageKey;
    }

    public Object[] messageArguments() {
        return Arrays.copyOf(messageArguments, messageArguments.length);
    }
}
