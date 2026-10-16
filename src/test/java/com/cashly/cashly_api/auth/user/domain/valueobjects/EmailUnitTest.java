package com.cashly.cashly_api.auth.user.domain.valueobjects;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.auth.user.domain.valueobjects.EmailUnitTest;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EmailUnitTest {

    @Test
    void should_CreateEmail_When_ValidEmailProvided() {
        String validEmail = "test@example.com";

        Email email = new Email(validEmail);

        assertNotNull(email);
        assertEquals("test@example.com", email.getValue());
    }

    @Test
    void should_NormalizeToLowercase_When_EmailCreated() {
        String mixedCaseEmail = "Test@Example.COM";

        Email email = new Email(mixedCaseEmail);

        assertEquals("test@example.com", email.getValue());
    }

    @Test
    void should_TrimWhitespace_When_EmailCreated() {
        String emailWithSpaces = "  test@example.com  ";

        Email email = new Email(emailWithSpaces);

        assertEquals("test@example.com", email.getValue());
    }

    @Test
    void should_ThrowException_When_NullEmailProvided() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Email(null)
        );
        assertEquals("Email cannot be null or empty", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_EmptyEmailProvided() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Email("   ")
        );
        assertEquals("Email cannot be null or empty", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid",
        "invalid@",
        "@invalid.com",
        "invalid@.com",
        "invalid@com",
        "invalid..email@example.com",
        "invalid email@example.com",
        "invalid@example",
        "invalid@.example.com"
    })
    void should_ThrowException_When_InvalidEmailFormatProvided(String invalidEmail) {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Email(invalidEmail)
        );
        assertTrue(exception.getMessage().contains("Invalid email format"));
    }

    @Test
    void should_ThrowException_When_EmailExceedsMaxLength() {
        String longEmail = "a".repeat(250) + "@example.com";

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Email(longEmail)
        );
        assertEquals("Email cannot exceed 255 characters", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "user@example.com",
        "user.name@example.com",
        "user+tag@example.co.uk",
        "user_name@example-domain.com",
        "123@example.com",
        "user@sub.example.com"
    })
    void should_AcceptValidEmailFormats(String validEmail) {
        Email email = new Email(validEmail);

        assertNotNull(email);
        assertEquals(validEmail.toLowerCase(), email.getValue());
    }

    @Test
    void should_BeEqual_When_SameEmailProvided() {
        Email email1 = new Email("test@example.com");
        Email email2 = new Email("test@example.com");

        assertEquals(email1, email2);
        assertEquals(email1.hashCode(), email2.hashCode());
    }

    @Test
    void should_BeEqual_When_DifferentCaseProvided() {
        Email email1 = new Email("Test@Example.com");
        Email email2 = new Email("test@example.com");

        assertEquals(email1, email2);
    }

    @Test
    void should_NotBeEqual_When_DifferentEmailProvided() {
        Email email1 = new Email("user1@example.com");
        Email email2 = new Email("user2@example.com");

        assertNotEquals(email1, email2);
    }

    @Test
    void should_HaveToString_When_EmailCreated() {
        Email email = new Email("test@example.com");

        String result = email.toString();

        assertNotNull(result);
        assertTrue(result.contains("Email"));
        assertTrue(result.contains("test@example.com"));
    }
}
