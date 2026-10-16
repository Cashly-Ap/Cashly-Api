package com.cashly.cashly_api.auth.user.domain.valueobjects;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Password;
import com.cashly.cashly_api.auth.user.domain.valueobjects.PasswordUnitTest;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUnitTest {

    @Test
    void should_CreatePassword_When_ValidHashedValueProvided() {
        String hashedPassword = "$2a$12$abcdefghijklmnopqrstuvwxyz123456789";

        Password password = Password.fromHash(hashedPassword);

        assertNotNull(password);
        assertEquals(hashedPassword, password.getHashedValue());
    }

    @Test
    void should_ThrowException_When_NullHashedValueProvided() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Password.fromHash(null)
        );
        assertEquals("Hashed password cannot be null or empty", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_EmptyHashedValueProvided() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Password.fromHash("   ")
        );
        assertEquals("Hashed password cannot be null or empty", exception.getMessage());
    }

    @Test
    void should_ValidateRawPassword_When_ValidPasswordProvided() {
        String validPassword = "StrongP@ss123";

        assertDoesNotThrow(() -> Password.validateRawPassword(validPassword));
    }

    @Test
    void should_ThrowException_When_NullRawPasswordProvided() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Password.validateRawPassword(null)
        );
        assertEquals("Password cannot be null or empty", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_EmptyRawPasswordProvided() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Password.validateRawPassword("")
        );
        assertEquals("Password cannot be null or empty", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_PasswordTooShort() {
        String shortPassword = "Pass1!";

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Password.validateRawPassword(shortPassword)
        );
        assertEquals("Password must be at least 8 characters long", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_PasswordTooLong() {
        String longPassword = "P@ssw0rd" + "a".repeat(100);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Password.validateRawPassword(longPassword)
        );
        assertEquals("Password cannot exceed 100 characters", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_NoUppercaseLetter() {
        String password = "password123!";

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Password.validateRawPassword(password)
        );
        assertEquals("Password must contain at least one uppercase letter", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_NoLowercaseLetter() {
        String password = "PASSWORD123!";

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Password.validateRawPassword(password)
        );
        assertEquals("Password must contain at least one lowercase letter", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_NoDigit() {
        String password = "Password!";

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Password.validateRawPassword(password)
        );
        assertEquals("Password must contain at least one digit", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_NoSpecialCharacter() {
        String password = "Password123";

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Password.validateRawPassword(password)
        );
        assertEquals("Password must contain at least one special character", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "StrongP@ss123",
        "MyP@ssw0rd!",
        "Test#1234Pass",
        "Secure$Pass99",
        "Complex&Pass1"
    })
    void should_AcceptValidPasswords(String validPassword) {
        assertDoesNotThrow(() -> Password.validateRawPassword(validPassword));
    }

    @Test
    void should_BeEqual_When_SameHashedValueProvided() {
        String hash = "$2a$12$abcdefghijklmnopqrstuvwxyz123456789";
        Password password1 = Password.fromHash(hash);
        Password password2 = Password.fromHash(hash);

        assertEquals(password1, password2);
        assertEquals(password1.hashCode(), password2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentHashedValueProvided() {
        Password password1 = Password.fromHash("$2a$12$hash1");
        Password password2 = Password.fromHash("$2a$12$hash2");

        assertNotEquals(password1, password2);
    }

    @Test
    void should_HidePasswordInToString_When_PasswordCreated() {
        Password password = Password.fromHash("$2a$12$secrethash");

        String result = password.toString();

        assertNotNull(result);
        assertTrue(result.contains("Password"));
        assertFalse(result.contains("secrethash"));
        assertTrue(result.contains("***"));
    }
}
