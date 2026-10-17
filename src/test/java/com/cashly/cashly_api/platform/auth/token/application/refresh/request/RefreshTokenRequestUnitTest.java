package com.cashly.cashly_api.platform.auth.token.application.refresh.request;

import com.cashly.cashly_api.platform.auth.token.application.refresh.request.RefreshTokenRequest;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenRequestUnitTest {

    @Test
    void should_CreateRefreshTokenRequest_When_ValidTokenProvided() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token-123");

        assertThat(request).isNotNull();
        assertThat(request.refreshToken()).isEqualTo("refresh-token-123");
    }

    @Test
    void should_ThrowException_When_RefreshTokenIsNull() {
        assertThatThrownBy(() -> new RefreshTokenRequest(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Refresh token is required");
    }

    @Test
    void should_ThrowException_When_RefreshTokenIsEmpty() {
        assertThatThrownBy(() -> new RefreshTokenRequest(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Refresh token is required");
    }

    @Test
    void should_ThrowException_When_RefreshTokenIsBlank() {
        assertThatThrownBy(() -> new RefreshTokenRequest("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Refresh token is required");
    }

    @Test
    void should_BeEqual_When_SameTokens() {
        RefreshTokenRequest request1 = new RefreshTokenRequest("refresh-token-123");
        RefreshTokenRequest request2 = new RefreshTokenRequest("refresh-token-123");

        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentTokens() {
        RefreshTokenRequest request1 = new RefreshTokenRequest("refresh-token-123");
        RefreshTokenRequest request2 = new RefreshTokenRequest("refresh-token-456");

        assertThat(request1).isNotEqualTo(request2);
    }
}
