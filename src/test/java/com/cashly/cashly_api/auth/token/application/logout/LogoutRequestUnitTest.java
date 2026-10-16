package com.cashly.cashly_api.auth.token.application.logout;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LogoutRequestUnitTest {

    @Test
    void should_CreateLogoutRequest_When_ValidTokenProvided() {
        LogoutRequest request = new LogoutRequest("refresh-token-123");

        assertThat(request).isNotNull();
        assertThat(request.refreshToken()).isEqualTo("refresh-token-123");
    }

    @Test
    void should_ThrowException_When_RefreshTokenIsNull() {
        assertThatThrownBy(() -> new LogoutRequest(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Refresh token is required");
    }

    @Test
    void should_ThrowException_When_RefreshTokenIsEmpty() {
        assertThatThrownBy(() -> new LogoutRequest(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Refresh token is required");
    }

    @Test
    void should_ThrowException_When_RefreshTokenIsBlank() {
        assertThatThrownBy(() -> new LogoutRequest("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Refresh token is required");
    }

    @Test
    void should_BeEqual_When_SameTokens() {
        LogoutRequest request1 = new LogoutRequest("refresh-token-123");
        LogoutRequest request2 = new LogoutRequest("refresh-token-123");

        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentTokens() {
        LogoutRequest request1 = new LogoutRequest("refresh-token-123");
        LogoutRequest request2 = new LogoutRequest("refresh-token-456");

        assertThat(request1).isNotEqualTo(request2);
    }
}
