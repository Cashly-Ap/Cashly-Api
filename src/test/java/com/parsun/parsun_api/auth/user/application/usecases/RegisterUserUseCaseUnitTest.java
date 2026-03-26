package com.parsun.parsun_api.auth.user.application.usecases;

import com.parsun.parsun_api.auth.user.application.dto.request.RegisterUserRequest;
import com.parsun.parsun_api.auth.user.application.dto.response.UserResponse;
import com.parsun.parsun_api.auth.user.domain.services.PasswordEncoder;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserRole;
import com.parsun.parsun_api.auth.user.application.exceptions.DuplicateEmailException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterUserUseCase Unit Tests")
class RegisterUserUseCaseUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserUseCase registerUserUseCase;

    private RegisterUserRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterUserRequest(
            "test@example.com",
            "Test User",
            "SecurePass123!",
            UserRole.CUSTOMER
        );
    }

    @Test
    @DisplayName("should_RegisterUser_When_ValidRequestProvided")
    void should_RegisterUser_When_ValidRequestProvided() {
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = registerUserUseCase.execute(validRequest);

        assertNotNull(response);
        assertEquals("test@example.com", response.email());
        assertTrue(response.active());
        assertFalse(response.emailVerified());

        verify(userRepository, times(1)).existsByEmail(any(Email.class));
        verify(passwordEncoder, times(1)).encode("SecurePass123!");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("should_ThrowException_When_EmailAlreadyExists")
    void should_ThrowException_When_EmailAlreadyExists() {
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

        DuplicateEmailException exception = assertThrows(
            DuplicateEmailException.class,
            () -> registerUserUseCase.execute(validRequest)
        );

        assertEquals("Email already registered: test@example.com", exception.getMessage());
        verify(userRepository, times(1)).existsByEmail(any(Email.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should_ThrowException_When_InvalidEmailProvided")
    void should_ThrowException_When_InvalidEmailProvided() {
        RegisterUserRequest invalidRequest = new RegisterUserRequest(
            "invalid-email",
            "Test User",
            "SecurePass123!",
            UserRole.CUSTOMER
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> registerUserUseCase.execute(invalidRequest)
        );

        verify(userRepository, never()).existsByEmail(any(Email.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should_ThrowException_When_WeakPasswordProvided")
    void should_ThrowException_When_WeakPasswordProvided() {
        RegisterUserRequest weakPasswordRequest = new RegisterUserRequest(
            "test@example.com",
            "Test User",
            "weak",
            UserRole.CUSTOMER
        );

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);

        assertThrows(
            IllegalArgumentException.class,
            () -> registerUserUseCase.execute(weakPasswordRequest)
        );

        verify(userRepository, times(1)).existsByEmail(any(Email.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should_HashPassword_When_RegisteringUser")
    void should_HashPassword_When_RegisteringUser() {
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123!")).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        registerUserUseCase.execute(validRequest);

        verify(passwordEncoder, times(1)).encode("SecurePass123!");
    }

    @Test
    @DisplayName("should_CreateActiveUser_When_Registering")
    void should_CreateActiveUser_When_Registering() {
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = registerUserUseCase.execute(validRequest);

        assertTrue(response.active());
    }

    @Test
    @DisplayName("should_CreateUnverifiedEmail_When_Registering")
    void should_CreateUnverifiedEmail_When_Registering() {
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = registerUserUseCase.execute(validRequest);

        assertFalse(response.emailVerified());
    }

    @Test
    @DisplayName("should_NormalizeEmail_When_Registering")
    void should_NormalizeEmail_When_Registering() {
        RegisterUserRequest upperCaseEmailRequest = new RegisterUserRequest(
            "TEST@EXAMPLE.COM",
            "Test User",
            "SecurePass123!",
            UserRole.CUSTOMER
        );

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = registerUserUseCase.execute(upperCaseEmailRequest);

        assertEquals("test@example.com", response.email());
    }

}
