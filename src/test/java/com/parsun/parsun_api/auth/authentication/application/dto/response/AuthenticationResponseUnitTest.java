package com.parsun.parsun_api.auth.authentication.application.dto.response;

import com.parsun.parsun_api.auth.authentication.domain.entities.AuthenticableType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationResponseUnitTest {

    @Test
    void should_CreateAuthenticationResponse_When_ValidDataProvided() {
        AuthenticatedEntityResponse entityResponse = new AuthenticatedEntityResponse(
            "1",
            "john@example.com",
            "John Doe",
            AuthenticableType.USER,
            true,
            null,
            LocalDateTime.now()
        );

        AuthenticationResponse response = new AuthenticationResponse(
            "access-token-123",
            "refresh-token-456",
            "Bearer",
            3600L,
            entityResponse
        );

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token-123");
        assertThat(response.refreshToken()).isEqualTo("refresh-token-456");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
        assertThat(response.entity()).isEqualTo(entityResponse);
    }

    @Test
    void should_CreateAuthenticationResponse_When_UsingFactoryMethod() {
        AuthenticatedEntityResponse entityResponse = new AuthenticatedEntityResponse(
            "1",
            "john@example.com",
            "John Doe",
            AuthenticableType.USER,
            true,
            null,
            LocalDateTime.now()
        );

        AuthenticationResponse response = AuthenticationResponse.of(
            "access-token-123",
            "refresh-token-456",
            3600L,
            entityResponse
        );

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token-123");
        assertThat(response.refreshToken()).isEqualTo("refresh-token-456");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
        assertThat(response.entity()).isEqualTo(entityResponse);
    }

    @Test
    void should_SetTokenTypeToBearer_When_UsingFactoryMethod() {
        AuthenticatedEntityResponse entityResponse = new AuthenticatedEntityResponse(
            "1",
            "john@example.com",
            "John Doe",
            AuthenticableType.USER,
            true,
            null,
            LocalDateTime.now()
        );

        AuthenticationResponse response = AuthenticationResponse.of(
            "access-token-123",
            "refresh-token-456",
            3600L,
            entityResponse
        );

        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void should_BeEqual_When_SameValues() {
        AuthenticatedEntityResponse entityResponse = new AuthenticatedEntityResponse(
            "1",
            "john@example.com",
            "John Doe",
            AuthenticableType.USER,
            true,
            null,
            LocalDateTime.of(2025, 12, 17, 12, 0)
        );

        AuthenticationResponse response1 = AuthenticationResponse.of(
            "access-token-123",
            "refresh-token-456",
            3600L,
            entityResponse
        );

        AuthenticationResponse response2 = AuthenticationResponse.of(
            "access-token-123",
            "refresh-token-456",
            3600L,
            entityResponse
        );

        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentAccessTokens() {
        AuthenticatedEntityResponse entityResponse = new AuthenticatedEntityResponse(
            "1",
            "john@example.com",
            "John Doe",
            AuthenticableType.USER,
            true,
            null,
            LocalDateTime.now()
        );

        AuthenticationResponse response1 = AuthenticationResponse.of(
            "access-token-123",
            "refresh-token-456",
            3600L,
            entityResponse
        );

        AuthenticationResponse response2 = AuthenticationResponse.of(
            "access-token-789",
            "refresh-token-456",
            3600L,
            entityResponse
        );

        assertThat(response1).isNotEqualTo(response2);
    }

    @Test
    void should_NotBeEqual_When_DifferentRefreshTokens() {
        AuthenticatedEntityResponse entityResponse = new AuthenticatedEntityResponse(
            "1",
            "john@example.com",
            "John Doe",
            AuthenticableType.USER,
            true,
            null,
            LocalDateTime.now()
        );

        AuthenticationResponse response1 = AuthenticationResponse.of(
            "access-token-123",
            "refresh-token-456",
            3600L,
            entityResponse
        );

        AuthenticationResponse response2 = AuthenticationResponse.of(
            "access-token-123",
            "refresh-token-789",
            3600L,
            entityResponse
        );

        assertThat(response1).isNotEqualTo(response2);
    }

    @Test
    void should_NotBeEqual_When_DifferentExpiresIn() {
        AuthenticatedEntityResponse entityResponse = new AuthenticatedEntityResponse(
            "1",
            "john@example.com",
            "John Doe",
            AuthenticableType.USER,
            true,
            null,
            LocalDateTime.now()
        );

        AuthenticationResponse response1 = AuthenticationResponse.of(
            "access-token-123",
            "refresh-token-456",
            3600L,
            entityResponse
        );

        AuthenticationResponse response2 = AuthenticationResponse.of(
            "access-token-123",
            "refresh-token-456",
            7200L,
            entityResponse
        );

        assertThat(response1).isNotEqualTo(response2);
    }

    @Test
    void should_HaveCorrectStringRepresentation() {
        AuthenticatedEntityResponse entityResponse = new AuthenticatedEntityResponse(
            "1",
            "john@example.com",
            "John Doe",
            AuthenticableType.USER,
            true,
            null,
            LocalDateTime.now()
        );

        AuthenticationResponse response = AuthenticationResponse.of(
            "access-token-123",
            "refresh-token-456",
            3600L,
            entityResponse
        );

        assertThat(response.toString()).contains("access-token-123");
        assertThat(response.toString()).contains("refresh-token-456");
        assertThat(response.toString()).contains("Bearer");
        assertThat(response.toString()).contains("3600");
    }
}
