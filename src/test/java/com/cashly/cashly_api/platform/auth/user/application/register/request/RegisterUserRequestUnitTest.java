package com.cashly.cashly_api.platform.auth.user.application.register.request;

import com.cashly.cashly_api.platform.auth.user.application.register.request.RegisterUserRequest;

import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterUserRequestUnitTest {

    @Test
    void should_CreateRegisterUserRequest_When_ValidDataProvided() {
        RegisterUserRequest request = new RegisterUserRequest(
            "user@example.com",
            "John Doe",
            "password123",
            UserRole.OPERATOR
        );

        assertThat(request).isNotNull();
        assertThat(request.email()).isEqualTo("user@example.com");
        assertThat(request.userName()).isEqualTo("John Doe");
        assertThat(request.password()).isEqualTo("password123");
        assertThat(request.role()).isEqualTo(UserRole.OPERATOR);
    }

    @Test
    void should_ThrowException_When_EmailIsNull() {
        assertThatThrownBy(() -> new RegisterUserRequest(null, "John Doe", "password123", UserRole.OPERATOR))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email is required");
    }

    @Test
    void should_ThrowException_When_EmailIsEmpty() {
        assertThatThrownBy(() -> new RegisterUserRequest("", "John Doe", "password123", UserRole.OPERATOR))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email is required");
    }

    @Test
    void should_ThrowException_When_EmailIsBlank() {
        assertThatThrownBy(() -> new RegisterUserRequest("   ", "John Doe", "password123", UserRole.OPERATOR))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email is required");
    }

    @Test
    void should_ThrowException_When_UserNameIsNull() {
        assertThatThrownBy(() -> new RegisterUserRequest("user@example.com", null, "password123", UserRole.OPERATOR))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User name is required");
    }

    @Test
    void should_ThrowException_When_UserNameIsEmpty() {
        assertThatThrownBy(() -> new RegisterUserRequest("user@example.com", "", "password123", UserRole.OPERATOR))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User name is required");
    }

    @Test
    void should_ThrowException_When_UserNameIsBlank() {
        assertThatThrownBy(() -> new RegisterUserRequest("user@example.com", "   ", "password123", UserRole.OPERATOR))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User name is required");
    }

    @Test
    void should_ThrowException_When_PasswordIsNull() {
        assertThatThrownBy(() -> new RegisterUserRequest("user@example.com", "John Doe", null, UserRole.OPERATOR))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Password is required");
    }

    @Test
    void should_ThrowException_When_PasswordIsEmpty() {
        assertThatThrownBy(() -> new RegisterUserRequest("user@example.com", "John Doe", "", UserRole.OPERATOR))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Password is required");
    }

    @Test
    void should_ThrowException_When_RoleIsNull() {
        assertThatThrownBy(() -> new RegisterUserRequest("user@example.com", "John Doe", "password123", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Role is required");
    }

    @Test
    void should_BeEqual_When_SameValues() {
        RegisterUserRequest request1 = new RegisterUserRequest(
            "user@example.com",
            "John Doe",
            "password123",
            UserRole.OPERATOR
        );

        RegisterUserRequest request2 = new RegisterUserRequest(
            "user@example.com",
            "John Doe",
            "password123",
            UserRole.OPERATOR
        );

        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentEmails() {
        RegisterUserRequest request1 = new RegisterUserRequest(
            "user1@example.com",
            "John Doe",
            "password123",
            UserRole.OPERATOR
        );

        RegisterUserRequest request2 = new RegisterUserRequest(
            "user2@example.com",
            "John Doe",
            "password123",
            UserRole.OPERATOR
        );

        assertThat(request1).isNotEqualTo(request2);
    }
}