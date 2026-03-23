package com.parsun.parsun_api.auth.authentication.application.dto.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginRequestUnitTest {

    @Test
    void should_CreateLoginRequest_When_ValidDataProvided() {
        LoginRequest request = new LoginRequest("user@example.com", "password123");

        assertThat(request).isNotNull();
        assertThat(request.email()).isEqualTo("user@example.com");
        assertThat(request.password()).isEqualTo("password123");
    }

    @Test
    void should_ThrowException_When_EmailIsNull() {
        assertThatThrownBy(() -> new LoginRequest(null, "password123"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email is required");
    }

    @Test
    void should_ThrowException_When_EmailIsEmpty() {
        assertThatThrownBy(() -> new LoginRequest("", "password123"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email is required");
    }

    @Test
    void should_ThrowException_When_EmailIsBlank() {
        assertThatThrownBy(() -> new LoginRequest("   ", "password123"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email is required");
    }

    @Test
    void should_ThrowException_When_PasswordIsNull() {
        assertThatThrownBy(() -> new LoginRequest("user@example.com", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Password is required");
    }

    @Test
    void should_ThrowException_When_PasswordIsEmpty() {
        assertThatThrownBy(() -> new LoginRequest("user@example.com", ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Password is required");
    }

    @Test
    void should_BeEqual_When_SameValues() {
        LoginRequest request1 = new LoginRequest("user@example.com", "password123");
        LoginRequest request2 = new LoginRequest("user@example.com", "password123");

        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentEmails() {
        LoginRequest request1 = new LoginRequest("user1@example.com", "password123");
        LoginRequest request2 = new LoginRequest("user2@example.com", "password123");

        assertThat(request1).isNotEqualTo(request2);
    }

    @Test
    void should_NotBeEqual_When_DifferentPasswords() {
        LoginRequest request1 = new LoginRequest("user@example.com", "password123");
        LoginRequest request2 = new LoginRequest("user@example.com", "password456");

        assertThat(request1).isNotEqualTo(request2);
    }

    @Test
    void should_HaveCorrectStringRepresentation() {
        LoginRequest request = new LoginRequest("user@example.com", "password123");

        assertThat(request.toString()).contains("user@example.com");
        assertThat(request.toString()).contains("password123");
    }
}
