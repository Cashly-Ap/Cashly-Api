package com.parsun.parsun_api.auth.user.application.dto.response;

import com.parsun.parsun_api.auth.user.domain.valueobjects.UserRole;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserResponseUnitTest {

    @Test
    void should_CreateUserResponse_When_ValidDataProvided() {
        LocalDateTime now = LocalDateTime.now();

        UserResponse response = new UserResponse(
            "1",
            "john@example.com",
            "John Doe",
            UserRole.CUSTOMER,
            true,
            false,
            new BigDecimal("1.00"),
            now
        );

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("1");
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.displayName()).isEqualTo("John Doe");
        assertThat(response.role()).isEqualTo(UserRole.CUSTOMER);
        assertThat(response.active()).isTrue();
        assertThat(response.emailVerified()).isFalse();
        assertThat(response.commissionDeductionPercentage()).isEqualByComparingTo("1.00");
        assertThat(response.createdAt()).isEqualTo(now);
    }

    @Test
    void should_BeEqual_When_SameValues() {
        LocalDateTime now = LocalDateTime.of(2025, 12, 17, 12, 0);

        UserResponse response1 = new UserResponse(
            "1",
            "john@example.com",
            "John Doe",
            UserRole.CUSTOMER,
            true,
            false,
            new BigDecimal("1.00"),
            now
        );

        UserResponse response2 = new UserResponse(
            "1",
            "john@example.com",
            "John Doe",
            UserRole.CUSTOMER,
            true,
            false,
            new BigDecimal("1.00"),
            now
        );

        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentIds() {
        LocalDateTime now = LocalDateTime.now();

        UserResponse response1 = new UserResponse(
            "1",
            "john@example.com",
            "John Doe",
            UserRole.CUSTOMER,
            true,
            false,
            new BigDecimal("1.00"),
            now
        );

        UserResponse response2 = new UserResponse(
            "2",
            "john@example.com",
            "John Doe",
            UserRole.CUSTOMER,
            true,
            false,
            new BigDecimal("1.00"),
            now
        );

        assertThat(response1).isNotEqualTo(response2);
    }

    @Test
    void should_NotBeEqual_When_DifferentEmails() {
        LocalDateTime now = LocalDateTime.now();

        UserResponse response1 = new UserResponse(
            "1",
            "john@example.com",
            "John Doe",
            UserRole.CUSTOMER,
            true,
            false,
            new BigDecimal("1.00"),
            now
        );

        UserResponse response2 = new UserResponse(
            "1",
            "jane@example.com",
            "John Doe",
            UserRole.CUSTOMER,
            true,
            false,
            new BigDecimal("1.00"),
            now
        );

        assertThat(response1).isNotEqualTo(response2);
    }

    @Test
    void should_HaveCorrectStringRepresentation() {
        LocalDateTime now = LocalDateTime.now();

        UserResponse response = new UserResponse(
            "1",
            "john@example.com",
            "John Doe",
            UserRole.CUSTOMER,
            true,
            false,
            new BigDecimal("1.00"),
            now
        );

        assertThat(response.toString()).contains("1");
        assertThat(response.toString()).contains("john@example.com");
        assertThat(response.toString()).contains("John Doe");
    }
}

