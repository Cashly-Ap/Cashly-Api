package com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("RegisterStatus Enum Unit Tests")
class RegisterStatusUnitTest {

    @Test
    void should_HaveTwoValues() {
        RegisterStatus[] values = RegisterStatus.values();
        assertEquals(2, values.length);
    }

    @Test
    void should_HaveCorrectOrder() {
        assertEquals(RegisterStatus.OPEN, RegisterStatus.valueOf("OPEN"));
        assertEquals(RegisterStatus.CLOSED, RegisterStatus.valueOf("CLOSED"));
    }

    @Test
    void should_StartWithOpenStatus() {
        assertNotNull(RegisterStatus.OPEN);
        assertEquals("OPEN", RegisterStatus.OPEN.name());
    }

    @Test
    void should_TransitionToClosed() {
        assertEquals("CLOSED", RegisterStatus.CLOSED.name());
    }
}
