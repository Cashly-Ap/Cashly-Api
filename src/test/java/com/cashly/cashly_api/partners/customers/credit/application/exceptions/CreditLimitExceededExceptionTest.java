package com.cashly.cashly_api.partners.customers.credit.application.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreditLimitExceededException Unit Tests")
class CreditLimitExceededExceptionTest {

    @Test
    void should_CreateException_When_MessageProvided() {
        String message = "Crédito insuficiente para Cliente Test";
        CreditLimitExceededException exception = new CreditLimitExceededException(
                message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void should_BeRuntimeException() {
        CreditLimitExceededException exception = new CreditLimitExceededException(
                "test");

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void should_AllowNullMessage() {
        CreditLimitExceededException exception = new CreditLimitExceededException(
                null);

        assertNull(exception.getMessage());
    }
}
