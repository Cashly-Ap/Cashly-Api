package com.cashly.cashly_api.platform.auth.token.application.logout.request.validation;

import com.cashly.cashly_api.platform.auth.token.application.logout.request.LogoutRequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LogoutRequest Unit Tests")
class LogoutRequestUnitTest {

    @Test
    @DisplayName("should_CreateLogoutRequest_When_ValidTokenProvided")
    void should_CreateLogoutRequest_When_ValidTokenProvided() {
        LogoutRequest request = new LogoutRequest("refresh-token");

        assertNotNull(request);
        assertEquals("refresh-token", request.refreshToken());
    }

    @Test
    @DisplayName("should_ThrowException_When_RefreshTokenIsNull")
    void should_ThrowException_When_RefreshTokenIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> new LogoutRequest(null));

        assertEquals("Refresh token is required", exception.getMessage());
    }

    @Test
    @DisplayName("should_ThrowException_When_RefreshTokenIsEmpty")
    void should_ThrowException_When_RefreshTokenIsEmpty() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> new LogoutRequest(""));

        assertEquals("Refresh token is required", exception.getMessage());
    }

    @Test
    @DisplayName("should_ThrowException_When_RefreshTokenIsBlank")
    void should_ThrowException_When_RefreshTokenIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> new LogoutRequest("   "));

        assertEquals("Refresh token is required", exception.getMessage());
    }

    @Test
    @DisplayName("should_BeEqual_When_TokenMatches")
    void should_BeEqual_When_TokenMatches() {
        LogoutRequest request1 = new LogoutRequest("refresh-token");
        LogoutRequest request2 = new LogoutRequest("refresh-token");

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    @DisplayName("should_BeImmutable_When_Created")
    void should_BeImmutable_When_Created() {
        LogoutRequest request = new LogoutRequest("refresh-token");

        assertEquals("refresh-token", request.refreshToken());
    }

    @Test
    @DisplayName("should_GenerateToString_When_Called")
    void should_GenerateToString_When_Called() {
        LogoutRequest request = new LogoutRequest("refresh-token");

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("refresh-token"));
    }
}
