package com.cashly.cashly_api.cash_register.sessions.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RegisterId Value Object Unit Tests")
class RegisterIdUnitTest {

    @Test
    void should_CreateRegisterId_When_ValidUuidProvided() {
        UUID uuid = UUID.randomUUID();
        RegisterId closureId = RegisterId.of(uuid);
        assertEquals(uuid, closureId.value());
    }

    @Test
    void should_GenerateUniqueIds() {
        RegisterId id1 = RegisterId.generate();
        RegisterId id2 = RegisterId.generate();
        assertNotEquals(id1, id2);
    }

    @Test
    void should_ThrowException_When_NullValueProvided() {
        assertThrows(NullPointerException.class, () -> new RegisterId(null));
    }

    @Test
    void should_BeEqual_When_SameUuid() {
        UUID uuid = UUID.randomUUID();
        RegisterId id1 = RegisterId.of(uuid);
        RegisterId id2 = RegisterId.of(uuid);
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }
}
