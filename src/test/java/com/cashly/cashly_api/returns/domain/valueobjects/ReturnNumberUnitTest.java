package com.cashly.cashly_api.returns.domain.valueobjects;

import com.cashly.cashly_api.returns.domain.valueobjects.ReturnNumber;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReturnNumber Value Object Unit Tests")
class ReturnNumberUnitTest {

    @Test
    void should_CreateReturnNumber_When_ValidValue() {
        ReturnNumber returnNumber = new ReturnNumber("RET-2026-0001");

        assertEquals("RET-2026-0001", returnNumber.value());
    }

    @Test
    void should_CreateReturnNumber_When_UsingOf() {
        ReturnNumber returnNumber = ReturnNumber.of(2026, 1);

        assertEquals("RET-2026-0001", returnNumber.value());
    }

    @Test
    void should_CreateReturnNumber_When_YearAndSequenceAreValid() {
        ReturnNumber returnNumber = ReturnNumber.of(2026, 42);

        assertEquals("RET-2026-0042", returnNumber.value());
    }

    @Test
    void should_CreateReturnNumber_When_SequenceIsLarge() {
        ReturnNumber returnNumber = ReturnNumber.of(2026, 9999);

        assertEquals("RET-2026-9999", returnNumber.value());
    }

    @Test
    void should_ThrowException_When_ValueIsNull() {
        assertThrows(NullPointerException.class, () -> new ReturnNumber(null));
    }

    @Test
    void should_ThrowException_When_ValueIsBlank() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ReturnNumber("  "));

        assertEquals("ReturnNumber cannot be blank", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_SequenceIsZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ReturnNumber.of(2026, 0));

        assertEquals("Sequence must be greater than zero", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_SequenceIsNegative() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ReturnNumber.of(2026, -1));

        assertEquals("Sequence must be greater than zero", exception.getMessage());
    }

    @Test
    void should_BeEqual_When_SameValue() {
        ReturnNumber rn1 = new ReturnNumber("RET-2026-0001");
        ReturnNumber rn2 = new ReturnNumber("RET-2026-0001");

        assertEquals(rn1, rn2);
        assertEquals(rn1.hashCode(), rn2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentValues() {
        ReturnNumber rn1 = new ReturnNumber("RET-2026-0001");
        ReturnNumber rn2 = new ReturnNumber("RET-2026-0002");

        assertNotEquals(rn1, rn2);
    }
}
