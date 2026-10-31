package com.cashly.cashly_api.partners.customers.profile.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerAliasUnitTest {

    @Test
    void should_create_valid_alias() {
        CustomerAlias alias = new CustomerAlias("Cashly");
        assertEquals("Cashly", alias.getValue());
    }

    @Test
    void should_create_minimum_length_alias() {
        CustomerAlias alias = new CustomerAlias("AB");
        assertEquals("AB", alias.getValue());
    }

    @Test
    void should_create_maximum_length_alias() {
        String maxAlias = "A".repeat(100);
        CustomerAlias alias = new CustomerAlias(maxAlias);
        assertEquals(100, alias.getValue().length());
    }

    @Test
    void should_trim_whitespace() {
        CustomerAlias alias = new CustomerAlias("  Cashly  ");
        assertEquals("Cashly", alias.getValue());
    }

    @Test
    void should_throw_on_null() {
        assertThrows(IllegalArgumentException.class,
                () -> new CustomerAlias(null));
    }

    @Test
    void should_throw_on_blank() {
        assertThrows(IllegalArgumentException.class,
                () -> new CustomerAlias("   "));
    }

    @Test
    void should_throw_on_empty() {
        assertThrows(IllegalArgumentException.class,
                () -> new CustomerAlias(""));
    }

    @Test
    void should_throw_on_single_char() {
        assertThrows(IllegalArgumentException.class,
                () -> new CustomerAlias("A"));
    }

    @Test
    void should_throw_on_too_long() {
        String tooLong = "A".repeat(101);
        assertThrows(IllegalArgumentException.class,
                () -> new CustomerAlias(tooLong));
    }

    @Test
    void should_be_equal_for_same_value() {
        CustomerAlias alias1 = new CustomerAlias("Cashly");
        CustomerAlias alias2 = new CustomerAlias("Cashly");
        assertEquals(alias1, alias2);
        assertEquals(alias1.hashCode(), alias2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_values() {
        CustomerAlias alias1 = new CustomerAlias("Cashly");
        CustomerAlias alias2 = new CustomerAlias("Other");
        assertNotEquals(alias1, alias2);
    }

    @Test
    void should_return_value_in_toString() {
        CustomerAlias alias = new CustomerAlias("Cashly");
        assertEquals("Cashly", alias.toString());
    }
}
