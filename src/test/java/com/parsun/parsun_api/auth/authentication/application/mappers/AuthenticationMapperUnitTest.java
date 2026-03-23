package com.parsun.parsun_api.auth.authentication.application.mappers;

import com.parsun.parsun_api.auth.authentication.application.dto.response.AuthenticationResponse;
import com.parsun.parsun_api.auth.authentication.domain.entities.Authenticable;
import com.parsun.parsun_api.auth.authentication.domain.entities.AuthenticableType;
import com.parsun.parsun_api.auth.token.domain.entities.RefreshToken;
import com.parsun.parsun_api.auth.token.domain.valueobjects.RefreshTokenId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("AuthenticationMapper Unit Tests")
class AuthenticationMapperUnitTest {

    private AuthenticationMapper authenticationMapper;
    private RefreshToken refreshToken;
    private Authenticable authenticable;
    private String subjectId;

    @BeforeEach
    void setUp() {
        authenticationMapper = new AuthenticationMapper();

        subjectId = "test-subject-id-123";

        RefreshTokenId tokenId = RefreshTokenId.generate();
        refreshToken = new RefreshToken(
            tokenId,
            subjectId,
            AuthenticableType.USER,
            "refresh-token-value",
            LocalDateTime.now().plusDays(7)
        );

        authenticable = mock(Authenticable.class);
        when(authenticable.getSubjectId()).thenReturn(subjectId);
        when(authenticable.getEmail()).thenReturn("test@example.com");
        when(authenticable.getDisplayName()).thenReturn("Test User");
        when(authenticable.getType()).thenReturn(AuthenticableType.USER);
        when(authenticable.isActive()).thenReturn(true);
        when(authenticable.getCreatedAt()).thenReturn(LocalDateTime.now());
    }

    @Test
    @DisplayName("should_CreateAuthenticationResponse_When_ValidDataProvided")
    void should_CreateAuthenticationResponse_When_ValidDataProvided() {
        String accessToken = "access-token";
        long expiresIn = 900000L;

        AuthenticationResponse response = authenticationMapper.toAuthenticationResponse(
            accessToken,
            refreshToken,
            expiresIn,
            authenticable
        );

        assertNotNull(response);
        assertEquals(accessToken, response.accessToken());
        assertEquals(refreshToken.getToken(), response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(expiresIn, response.expiresIn());
        assertEquals(subjectId, response.entity().id());
    }

    @Test
    @DisplayName("should_SetBearerTokenType_When_Mapping")
    void should_SetBearerTokenType_When_Mapping() {
        AuthenticationResponse response = authenticationMapper.toAuthenticationResponse(
            "access-token",
            refreshToken,
            900000L,
            authenticable
        );

        assertEquals("Bearer", response.tokenType());
    }

    @Test
    @DisplayName("should_IncludeRefreshToken_When_Mapping")
    void should_IncludeRefreshToken_When_Mapping() {
        AuthenticationResponse response = authenticationMapper.toAuthenticationResponse(
            "access-token",
            refreshToken,
            900000L,
            authenticable
        );

        assertNotNull(response.refreshToken());
        assertEquals(refreshToken.getToken(), response.refreshToken());
    }

    @Test
    @DisplayName("should_IncludeEntityResponse_When_Mapping")
    void should_IncludeEntityResponse_When_Mapping() {
        AuthenticationResponse response = authenticationMapper.toAuthenticationResponse(
            "access-token",
            refreshToken,
            900000L,
            authenticable
        );

        assertNotNull(response.entity());
        assertEquals(subjectId, response.entity().id());
        assertEquals("test@example.com", response.entity().email());
        assertEquals("Test User", response.entity().displayName());
        assertEquals(AuthenticableType.USER, response.entity().type());
        assertTrue(response.entity().active());
    }

    @Test
    @DisplayName("should_IncludeExpirationTime_When_Mapping")
    void should_IncludeExpirationTime_When_Mapping() {
        long expiresIn = 1800000L;

        AuthenticationResponse response = authenticationMapper.toAuthenticationResponse(
            "access-token",
            refreshToken,
            expiresIn,
            authenticable
        );

        assertEquals(expiresIn, response.expiresIn());
    }

    @Test
    @DisplayName("should_MapAllFields_When_ConvertingToAuthenticationResponse")
    void should_MapAllFields_When_ConvertingToAuthenticationResponse() {
        String accessToken = "test-access-token";
        long expiresIn = 600000L;

        AuthenticationResponse response = authenticationMapper.toAuthenticationResponse(
            accessToken,
            refreshToken,
            expiresIn,
            authenticable
        );

        assertNotNull(response);
        assertNotNull(response.accessToken());
        assertNotNull(response.refreshToken());
        assertNotNull(response.tokenType());
        assertTrue(response.expiresIn() > 0);
        assertNotNull(response.entity());
    }
}
