package com.cashly.cashly_api.partners.customers.profile.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerCodeUnitTest {

    @Test
    void should_create_valid_positive_code() {
        CustomerCode code = new CustomerCode(1L);
        assertEquals(1L, code.getValue());
    }

    @Test
    void should_create_valid_large_code() {
        CustomerCode code = new CustomerCode(999999L);
        assertEquals(999999L, code.getValue());
    }

    @Test
    void should_throw_on_null() {
        assertThrows(IllegalArgumentException.class, () -> new CustomerCode(null));
    }

    @Test
    void should_throw_on_zero() {
        assertThrows(IllegalArgumentException.class, () -> new CustomerCode(0L));
    }

    @Test
    void should_throw_on_negative() {
        assertThrows(IllegalArgumentException.class, () -> new CustomerCode(-1L));
    }

    @Test
    void should_be_equal_for_same_value() {
        CustomerCode code1 = new CustomerCode(123L);
        CustomerCode code2 = new CustomerCode(123L);
        assertEquals(code1, code2);
        assertEquals(code1.hashCode(), code2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_values() {
        CustomerCode code1 = new CustomerCode(123L);
        CustomerCode code2 = new CustomerCode(456L);
        assertNotEquals(code1, code2);
    }

    @Test
    void should_return_value_in_toString() {
        CustomerCode code = new CustomerCode(123L);
        assertEquals("123", code.toString());
    }
}
