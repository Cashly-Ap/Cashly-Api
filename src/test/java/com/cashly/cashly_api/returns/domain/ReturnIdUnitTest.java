package com.cashly.cashly_api.returns.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReturnId Value Object Unit Tests")
class ReturnIdUnitTest {

    @Test
    void should_CreateReturnId_When_ValidUUID() {
        UUID value = UUID.randomUUID();
        ReturnId returnId = ReturnId.of(value);

        assertEquals(value, returnId.value());
    }

    @Test
    void should_GenerateReturnId_When_CallingGenerate() {
        ReturnId returnId = ReturnId.generate();

        assertNotNull(returnId.value());
    }

    @Test
    void should_GenerateUniqueIds_When_CallingGenerateMultipleTimes() {
        ReturnId id1 = ReturnId.generate();
        ReturnId id2 = ReturnId.generate();

        assertNotEquals(id1, id2);
    }

    @Test
    void should_ThrowException_When_ValueIsNull() {
        assertThrows(NullPointerException.class, () -> ReturnId.of(null));
    }

    @Test
    void should_BeEqual_When_SameValue() {
        UUID value = UUID.randomUUID();
        ReturnId id1 = ReturnId.of(value);
        ReturnId id2 = ReturnId.of(value);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentValues() {
        ReturnId id1 = ReturnId.generate();
        ReturnId id2 = ReturnId.generate();

        assertNotEquals(id1, id2);
    }
}
