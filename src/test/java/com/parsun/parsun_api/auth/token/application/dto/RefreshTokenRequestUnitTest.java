package com.parsun.parsun_api.auth.token.application.dto;

import com.parsun.parsun_api.auth.token.application.dto.request.RefreshTokenRequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RefreshTokenRequest Unit Tests")
class RefreshTokenRequestUnitTest {

    @Test
    @DisplayName("should_CreateRefreshTokenRequest_When_ValidTokenProvided")
    void should_CreateRefreshTokenRequest_When_ValidTokenProvided() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");

        assertNotNull(request);
        assertEquals("refresh-token", request.refreshToken());
    }

    @Test
    @DisplayName("should_ThrowException_When_RefreshTokenIsNull")
    void should_ThrowException_When_RefreshTokenIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new RefreshTokenRequest(null)
        );

        assertEquals("Refresh token is required", exception.getMessage());
    }

    @Test
    @DisplayName("should_ThrowException_When_RefreshTokenIsEmpty")
    void should_ThrowException_When_RefreshTokenIsEmpty() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new RefreshTokenRequest("")
        );

        assertEquals("Refresh token is required", exception.getMessage());
    }

    @Test
    @DisplayName("should_ThrowException_When_RefreshTokenIsBlank")
    void should_ThrowException_When_RefreshTokenIsBlank() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new RefreshTokenRequest("   ")
        );

        assertEquals("Refresh token is required", exception.getMessage());
    }

    @Test
    @DisplayName("should_BeEqual_When_TokenMatches")
    void should_BeEqual_When_TokenMatches() {
        RefreshTokenRequest request1 = new RefreshTokenRequest("refresh-token");
        RefreshTokenRequest request2 = new RefreshTokenRequest("refresh-token");

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    @DisplayName("should_BeImmutable_When_Created")
    void should_BeImmutable_When_Created() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");

        assertEquals("refresh-token", request.refreshToken());
    }

    @Test
    @DisplayName("should_GenerateToString_When_Called")
    void should_GenerateToString_When_Called() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("refresh-token"));
    }
}
