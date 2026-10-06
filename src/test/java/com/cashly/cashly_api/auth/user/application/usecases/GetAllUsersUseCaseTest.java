package com.cashly.cashly_api.auth.user.application.usecases;

import com.cashly.cashly_api.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.auth.user.domain.entities.User;
import com.cashly.cashly_api.auth.user.domain.repositories.UserRepository;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Password;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserName;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserRole;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAllUsersUseCase Tests")
class GetAllUsersUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetAllUsersUseCase useCase;

    private User testUser1;
    private User testUser2;
    private String authenticatedUserId;

    @BeforeEach
    void setUp() {
        authenticatedUserId = "auth-user-id";
        testUser1 = new User(
            UserId.generate(),
            new Email("user1@example.com"),
            new UserName("User One"),
            Password.fromHash("$2a$10$hashedPassword")
        );
        testUser2 = new User(
            UserId.generate(),
            new Email("user2@example.com"),
            new UserName("User Two"),
            Password.fromHash("$2a$10$hashedPassword")
        );
    }

    @Test
    @DisplayName("should_ReturnPaginatedUsers_When_UsersExist")
    void should_ReturnPaginatedUsers_When_UsersExist() {
        PageResponse<User> userPage = new PageResponse<>(
            List.of(testUser1, testUser2),
            2L, 1, 10, 0, true, true, false
        );

        when(userRepository.findAllWithFilters(eq(null), eq(null), eq(true), eq(authenticatedUserId),
                eq(0), eq(10), eq("name"), eq("asc")))
            .thenReturn(userPage);

        PageResponse<UserResponse> result = useCase.execute(authenticatedUserId, null, null,
            null, 0, 10, "name", "asc");

        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals(2L, result.totalElements());
        assertEquals("user1@example.com", result.content().get(0).email());
        assertEquals("user2@example.com", result.content().get(1).email());
        verify(userRepository).findAllWithFilters(eq(null), eq(null), eq(true), eq(authenticatedUserId),
            eq(0), eq(10), eq("name"), eq("asc"));
    }

    @Test
    @DisplayName("should_ReturnEmptyPage_When_NoUsersFound")
    void should_ReturnEmptyPage_When_NoUsersFound() {
        PageResponse<User> emptyPage = new PageResponse<>(
            List.of(), 0L, 0, 10, 0, true, true, true
        );

        when(userRepository.findAllWithFilters(eq(null), eq(null), eq(true), eq(authenticatedUserId),
                eq(0), eq(10), eq("name"), eq("asc")))
            .thenReturn(emptyPage);

        PageResponse<UserResponse> result = useCase.execute(authenticatedUserId, null, null,
            null, 0, 10, "name", "asc");

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0L, result.totalElements());
        assertTrue(result.empty());
    }

    @Test
    @DisplayName("should_UseDefaultActiveTrue_When_ActiveIsNull")
    void should_UseDefaultActiveTrue_When_ActiveIsNull() {
        PageResponse<User> emptyPage = new PageResponse<>(
            List.of(), 0L, 0, 10, 0, true, true, true
        );

        when(userRepository.findAllWithFilters(eq(null), eq(null), eq(true), eq(authenticatedUserId),
                eq(0), eq(10), eq("name"), eq("asc")))
            .thenReturn(emptyPage);

        useCase.execute(authenticatedUserId, null, null, null, 0, 10, "name", "asc");

        verify(userRepository).findAllWithFilters(eq(null), eq(null), eq(true), eq(authenticatedUserId),
            eq(0), eq(10), eq("name"), eq("asc"));
    }

    @Test
    @DisplayName("should_PassRoleFilter_When_RoleProvided")
    void should_PassRoleFilter_When_RoleProvided() {
        PageResponse<User> emptyPage = new PageResponse<>(
            List.of(), 0L, 0, 10, 0, true, true, true
        );

        when(userRepository.findAllWithFilters(eq(null), eq(UserRole.ADMIN), eq(true), eq(authenticatedUserId),
                eq(0), eq(10), eq("name"), eq("asc")))
            .thenReturn(emptyPage);

        PageResponse<UserResponse> result = useCase.execute(authenticatedUserId, UserRole.ADMIN, null,
            null, 0, 10, "name", "asc");

        assertNotNull(result);
        verify(userRepository).findAllWithFilters(eq(null), eq(UserRole.ADMIN), eq(true), eq(authenticatedUserId),
            eq(0), eq(10), eq("name"), eq("asc"));
    }

    @Test
    @DisplayName("should_PassSearchFilter_When_SearchProvided")
    void should_PassSearchFilter_When_SearchProvided() {
        PageResponse<User> emptyPage = new PageResponse<>(
            List.of(), 0L, 0, 10, 0, true, true, true
        );

        when(userRepository.findAllWithFilters(eq("test"), eq(null), eq(true), eq(authenticatedUserId),
                eq(0), eq(10), eq("name"), eq("asc")))
            .thenReturn(emptyPage);

        PageResponse<UserResponse> result = useCase.execute(authenticatedUserId, null, null,
            "test", 0, 10, "name", "asc");

        assertNotNull(result);
        verify(userRepository).findAllWithFilters(eq("test"), eq(null), eq(true), eq(authenticatedUserId),
            eq(0), eq(10), eq("name"), eq("asc"));
    }

    @Test
    @DisplayName("should_PassActiveFalse_When_ActiveIsFalse")
    void should_PassActiveFalse_When_ActiveIsFalse() {
        PageResponse<User> emptyPage = new PageResponse<>(
            List.of(), 0L, 0, 10, 0, true, true, true
        );

        when(userRepository.findAllWithFilters(eq(null), eq(null), eq(false), eq(authenticatedUserId),
                eq(0), eq(10), eq("name"), eq("asc")))
            .thenReturn(emptyPage);

        PageResponse<UserResponse> result = useCase.execute(authenticatedUserId, null, false,
            null, 0, 10, "name", "asc");

        assertNotNull(result);
        verify(userRepository).findAllWithFilters(eq(null), eq(null), eq(false), eq(authenticatedUserId),
            eq(0), eq(10), eq("name"), eq("asc"));
    }
}
