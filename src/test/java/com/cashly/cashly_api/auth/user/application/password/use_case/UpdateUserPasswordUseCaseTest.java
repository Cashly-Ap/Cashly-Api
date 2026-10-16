package com.cashly.cashly_api.auth.user.application.password.use_case;

import com.cashly.cashly_api.auth.user.application.password.request.UpdateUserPasswordRequest;
import com.cashly.cashly_api.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.auth.user.application.exceptions.UserNotFoundException;
import com.cashly.cashly_api.auth.user.domain.entity.User;
import com.cashly.cashly_api.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.auth.user.domain.services.PasswordEncoder;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Password;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserName;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateUserPasswordUseCase Tests")
class UpdateUserPasswordUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UpdateUserPasswordUseCase useCase;

    private User testUser;
    private String userId;

    @BeforeEach
    void setUp() {
        UUID uuid = UUID.randomUUID();
        userId = uuid.toString();
        testUser = new User(
            new UserId(uuid),
            new Email("test@example.com"),
            new UserName("Test User"),
            Password.fromHash("$2a$10$oldHashedPassword")
        );
    }

    @Test
    @DisplayName("should_UpdatePassword_When_UserExists")
    void should_UpdatePassword_When_UserExists() {
        UpdateUserPasswordRequest request = new UpdateUserPasswordRequest("OldPass123!", "NewSecurePass123!");

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPass123!", "$2a$10$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewSecurePass123!")).thenReturn("$2a$10$newHashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = useCase.execute(userId, request);

        assertNotNull(response);
        verify(passwordEncoder).encode("NewSecurePass123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("should_ThrowUserNotFoundException_When_UserDoesNotExist")
    void should_ThrowUserNotFoundException_When_UserDoesNotExist() {
        UpdateUserPasswordRequest request = new UpdateUserPasswordRequest("OldPass123!", "NewSecurePass123!");

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> useCase.execute(userId, request)
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findById(any(UserId.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should_EncodePassword_When_UpdatingPassword")
    void should_EncodePassword_When_UpdatingPassword() {
        UpdateUserPasswordRequest request = new UpdateUserPasswordRequest("OldPass123!", "NewSecurePass123!");

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPass123!", "$2a$10$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewSecurePass123!")).thenReturn("$2a$10$encodedHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(userId, request);

        verify(passwordEncoder).encode("NewSecurePass123!");
    }
}
