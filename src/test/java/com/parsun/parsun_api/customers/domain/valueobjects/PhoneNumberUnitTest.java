package com.parsun.parsun_api.customers.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhoneNumberUnitTest {

    @Test
    void should_create_valid_phone() {
        PhoneNumber phone = new PhoneNumber("+58-212-555-1234");
        assertEquals("+58-212-555-1234", phone.getValue());
    }

    @Test
    void should_create_simple_phone() {
        PhoneNumber phone = new PhoneNumber("5551234");
        assertEquals("5551234", phone.getValue());
    }

    @Test
    void should_trim_whitespace() {
        PhoneNumber phone = new PhoneNumber("  +58-212-555-1234  ");
        assertEquals("+58-212-555-1234", phone.getValue());
    }

    @Test
    void should_throw_on_null() {
        assertThrows(IllegalArgumentException.class, () -> new PhoneNumber(null));
    }

    @Test
    void should_throw_on_blank() {
        assertThrows(IllegalArgumentException.class, () -> new PhoneNumber("   "));
    }

    @Test
    void should_throw_on_empty() {
        assertThrows(IllegalArgumentException.class, () -> new PhoneNumber(""));
    }

    @Test
    void should_be_equal_for_same_value() {
        PhoneNumber phone1 = new PhoneNumber("+58-212-555-1234");
        PhoneNumber phone2 = new PhoneNumber("+58-212-555-1234");
        assertEquals(phone1, phone2);
        assertEquals(phone1.hashCode(), phone2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_values() {
        PhoneNumber phone1 = new PhoneNumber("+58-212-555-1234");
        PhoneNumber phone2 = new PhoneNumber("+58-212-555-5678");
        assertNotEquals(phone1, phone2);
    }

    @Test
    void should_return_value_in_toString() {
        PhoneNumber phone = new PhoneNumber("+58-212-555-1234");
        assertEquals("+58-212-555-1234", phone.toString());
    }
}
