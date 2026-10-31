package com.cashly.cashly_api.partners.suppliers.domain.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class EmailAddressUnitTest {

    @ParameterizedTest
    @CsvSource({"test@example.com, test@example.com",
            "user.name@domain.org, user.name@domain.org",
            "TEST@EXAMPLE.COM, test@example.com",
            "  test@example.com  , test@example.com"})
    void should_create_valid_email(String input, String expected) {
        EmailAddress email = new EmailAddress(input);
        assertEquals(expected, email.getValue());
    }

    @Test
    void should_throw_on_null() {
        assertThrows(IllegalArgumentException.class,
                () -> new EmailAddress(null));
    }

    @Test
    void should_throw_on_blank() {
        assertThrows(IllegalArgumentException.class,
                () -> new EmailAddress("   "));
    }

    @Test
    void should_throw_on_empty() {
        assertThrows(IllegalArgumentException.class,
                () -> new EmailAddress(""));
    }

    @Test
    void should_throw_on_no_at_sign() {
        assertThrows(IllegalArgumentException.class,
                () -> new EmailAddress("testexample.com"));
    }

    @Test
    void should_throw_on_no_domain() {
        assertThrows(IllegalArgumentException.class,
                () -> new EmailAddress("test@"));
    }

    @Test
    void should_throw_on_spaces_in_email() {
        assertThrows(IllegalArgumentException.class,
                () -> new EmailAddress("test @example.com"));
    }

    @Test
    void should_lowercase_email() {
        EmailAddress email = new EmailAddress("TEST@EXAMPLE.COM");
        assertEquals("test@example.com", email.getValue());
    }

    @Test
    void should_trim_whitespace() {
        EmailAddress email = new EmailAddress("  test@example.com  ");
        assertEquals("test@example.com", email.getValue());
    }

    @Test
    void should_be_equal_for_same_value() {
        EmailAddress email1 = new EmailAddress("test@example.com");
        EmailAddress email2 = new EmailAddress("test@example.com");
        assertEquals(email1, email2);
        assertEquals(email1.hashCode(), email2.hashCode());
    }

    @Test
    void should_be_equal_for_different_case() {
        EmailAddress email1 = new EmailAddress("test@example.com");
        EmailAddress email2 = new EmailAddress("TEST@EXAMPLE.COM");
        assertEquals(email1, email2);
    }

    @Test
    void should_not_be_equal_for_different_values() {
        EmailAddress email1 = new EmailAddress("test@example.com");
        EmailAddress email2 = new EmailAddress("other@example.com");
        assertNotEquals(email1, email2);
    }

    @Test
    void should_return_value_in_toString() {
        EmailAddress email = new EmailAddress("test@example.com");
        assertEquals("test@example.com", email.toString());
    }
}
