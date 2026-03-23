package com.parsun.parsun_api.auth.user.infrastructure.config;

import com.parsun.parsun_api.auth.user.infrastructure.config.BcryptPasswordEncoder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BcryptPasswordEncoder Unit Tests")
class BcryptPasswordEncoderUnitTest {

    private BcryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BcryptPasswordEncoder();
    }

    @Test
    @DisplayName("should_EncodePassword_When_ValidPasswordProvided")
    void should_EncodePassword_When_ValidPasswordProvided() {
        String rawPassword = "MySecurePassword123!";

        String encoded = passwordEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());
        assertNotEquals(rawPassword, encoded);
        assertTrue(encoded.startsWith("$2a$")); 
    }

    @Test
    @DisplayName("should_ThrowException_When_EncodingNullPassword")
    void should_ThrowException_When_EncodingNullPassword() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> passwordEncoder.encode(null)
        );

        assertEquals("Raw password cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("should_ThrowException_When_EncodingEmptyPassword")
    void should_ThrowException_When_EncodingEmptyPassword() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> passwordEncoder.encode("")
        );

        assertEquals("Raw password cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("should_MatchPasswords_When_RawAndEncodedAreTheSame")
    void should_MatchPasswords_When_RawAndEncodedAreTheSame() {
        String rawPassword = "MySecurePassword123!";
        String encoded = passwordEncoder.encode(rawPassword);

        boolean matches = passwordEncoder.matches(rawPassword, encoded);

        assertTrue(matches);
    }

    @Test
    @DisplayName("should_NotMatchPasswords_When_RawAndEncodedAreDifferent")
    void should_NotMatchPasswords_When_RawAndEncodedAreDifferent() {
        String rawPassword = "MySecurePassword123!";
        String wrongPassword = "WrongPassword456!";
        String encoded = passwordEncoder.encode(rawPassword);

        boolean matches = passwordEncoder.matches(wrongPassword, encoded);

        assertFalse(matches);
    }

    @Test
    @DisplayName("should_ReturnFalse_When_MatchingWithNullRawPassword")
    void should_ReturnFalse_When_MatchingWithNullRawPassword() {
        String encoded = passwordEncoder.encode("SomePassword123!");

        boolean matches = passwordEncoder.matches(null, encoded);

        assertFalse(matches);
    }

    @Test
    @DisplayName("should_ReturnFalse_When_MatchingWithNullEncodedPassword")
    void should_ReturnFalse_When_MatchingWithNullEncodedPassword() {
        String rawPassword = "SomePassword123!";

        boolean matches = passwordEncoder.matches(rawPassword, null);

        assertFalse(matches);
    }

    @Test
    @DisplayName("should_ReturnFalse_When_BothPasswordsAreNull")
    void should_ReturnFalse_When_BothPasswordsAreNull() {
        boolean matches = passwordEncoder.matches(null, null);

        assertFalse(matches);
    }

    @Test
    @DisplayName("should_GenerateDifferentHashes_When_EncodingSamePasswordTwice")
    void should_GenerateDifferentHashes_When_EncodingSamePasswordTwice() {
        String rawPassword = "MySecurePassword123!";

        String encoded1 = passwordEncoder.encode(rawPassword);
        String encoded2 = passwordEncoder.encode(rawPassword);

        assertNotEquals(encoded1, encoded2); 
        assertTrue(passwordEncoder.matches(rawPassword, encoded1));
        assertTrue(passwordEncoder.matches(rawPassword, encoded2));
    }

    @Test
    @DisplayName("should_EncodeSpecialCharacters_When_PasswordContainsSpecialChars")
    void should_EncodeSpecialCharacters_When_PasswordContainsSpecialChars() {
        String rawPassword = "P@$$w0rd!#$%^&*()";

        String encoded = passwordEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
    }

    @Test
    @DisplayName("should_HandleLongPasswords_When_PasswordIsVeryLong")
    void should_HandleLongPasswords_When_PasswordIsVeryLong() {
        String rawPassword = "A".repeat(70) + "1!";

        String encoded = passwordEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
    }

    @Test
    @DisplayName("should_HandleShortPasswords_When_PasswordIsShort")
    void should_HandleShortPasswords_When_PasswordIsShort() {
        String rawPassword = "Ab1!";

        String encoded = passwordEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
    }

    @Test
    @DisplayName("should_BeSecure_When_EncodingContainsSaltAndCost")
    void should_BeSecure_When_EncodingContainsSaltAndCost() {
        String rawPassword = "SecurePassword123!";

        String encoded = passwordEncoder.encode(rawPassword);

        assertTrue(encoded.startsWith("$2a$10$")); 
        assertTrue(encoded.length() >= 60); 
    }
}
