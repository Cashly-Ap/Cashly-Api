package com.cashly.cashly_api.customers.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LegalNameUnitTest {

    @Test
    void should_create_valid_name() {
        LegalName name = new LegalName("Cashly C.A.");
        assertEquals("Cashly C.A.", name.getValue());
    }

    @Test
    void should_create_minimum_length_name() {
        LegalName name = new LegalName("AB");
        assertEquals("AB", name.getValue());
    }

    @Test
    void should_create_maximum_length_name() {
        String maxName = "A".repeat(200);
        LegalName name = new LegalName(maxName);
        assertEquals(200, name.getValue().length());
    }

    @Test
    void should_trim_whitespace() {
        LegalName name = new LegalName("  Cashly C.A.  ");
        assertEquals("Cashly C.A.", name.getValue());
    }

    @Test
    void should_throw_on_null() {
        assertThrows(IllegalArgumentException.class, () -> new LegalName(null));
    }

    @Test
    void should_throw_on_blank() {
        assertThrows(IllegalArgumentException.class, () -> new LegalName("   "));
    }

    @Test
    void should_throw_on_empty() {
        assertThrows(IllegalArgumentException.class, () -> new LegalName(""));
    }

    @Test
    void should_throw_on_single_char() {
        assertThrows(IllegalArgumentException.class, () -> new LegalName("A"));
    }

    @Test
    void should_throw_on_too_long() {
        String tooLong = "A".repeat(201);
        assertThrows(IllegalArgumentException.class, () -> new LegalName(tooLong));
    }

    @Test
    void should_be_equal_for_same_value() {
        LegalName name1 = new LegalName("Cashly C.A.");
        LegalName name2 = new LegalName("Cashly C.A.");
        assertEquals(name1, name2);
        assertEquals(name1.hashCode(), name2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_values() {
        LegalName name1 = new LegalName("Cashly C.A.");
        LegalName name2 = new LegalName("Other C.A.");
        assertNotEquals(name1, name2);
    }

    @Test
    void should_return_value_in_toString() {
        LegalName name = new LegalName("Cashly C.A.");
        assertEquals("Cashly C.A.", name.toString());
    }
}
