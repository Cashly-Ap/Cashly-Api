package com.cashly.cashly_api.platform.auth.user.application.register.use_case;

import com.cashly.cashly_api.platform.auth.user.application.register.request.RegisterUserRequest;
import com.cashly.cashly_api.platform.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.platform.auth.user.domain.services.PasswordEncoder;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserRole;
import com.cashly.cashly_api.platform.auth.user.application.exceptions.DuplicateEmailException;
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

    @Mock
    private com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService access;

    @Mock
    private com.cashly.cashly_api.platform.auth.preferences.domain.UserPreferencesRepository preferences;

    @InjectMocks
    private RegisterUserUseCase registerUserUseCase;

    private RegisterUserRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterUserRequest("test@example.com", "Test User",
                "SecurePass123!", "John", "Doe", null, null,
                java.util.UUID
                        .fromString("10000000-0000-0000-0000-000000000001"),
                java.util.UUID
                        .fromString("20000000-0000-0000-0000-000000000001"));
    }

    @Test
    @DisplayName("should_RegisterUser_When_ValidRequestProvided")
    void should_RegisterUser_When_ValidRequestProvided() {
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = registerUserUseCase.execute(validRequest);

        assertNotNull(response);
        assertEquals("test@example.com", response.email());
        assertEquals("es", response.language());
        verify(preferences)
                .updateLanguage(java.util.UUID.fromString(response.id()), "es");
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
                () -> registerUserUseCase.execute(validRequest));

        assertEquals("Email already registered: test@example.com",
                exception.getMessage());
        verify(userRepository, times(1)).existsByEmail(any(Email.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should_ThrowException_When_InvalidEmailProvided")
    void should_ThrowException_When_InvalidEmailProvided() {
        RegisterUserRequest invalidRequest = new RegisterUserRequest(
                "invalid-email", "Test User", "SecurePass123!", "John", "Doe",
                null, null,
                java.util.UUID
                        .fromString("10000000-0000-0000-0000-000000000001"),
                java.util.UUID
                        .fromString("20000000-0000-0000-0000-000000000001"));

        assertThrows(IllegalArgumentException.class,
                () -> registerUserUseCase.execute(invalidRequest));

        verify(userRepository, never()).existsByEmail(any(Email.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should_ThrowException_When_WeakPasswordProvided")
    void should_ThrowException_When_WeakPasswordProvided() {
        RegisterUserRequest weakPasswordRequest = new RegisterUserRequest(
                "test@example.com", "Test User", "weak", "John", "Doe", null,
                null,
                java.util.UUID
                        .fromString("10000000-0000-0000-0000-000000000001"),
                java.util.UUID
                        .fromString("20000000-0000-0000-0000-000000000001"));

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> registerUserUseCase.execute(weakPasswordRequest));

        verify(userRepository, times(1)).existsByEmail(any(Email.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should_HashPassword_When_RegisteringUser")
    void should_HashPassword_When_RegisteringUser() {
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123!"))
                .thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        registerUserUseCase.execute(validRequest);

        verify(passwordEncoder, times(1)).encode("SecurePass123!");
    }

    @Test
    @DisplayName("should_CreateActiveUser_When_Registering")
    void should_CreateActiveUser_When_Registering() {
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = registerUserUseCase.execute(validRequest);

        assertTrue(response.active());
    }

    @Test
    @DisplayName("should_CreateUnverifiedEmail_When_Registering")
    void should_CreateUnverifiedEmail_When_Registering() {
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = registerUserUseCase.execute(validRequest);

        assertFalse(response.emailVerified());
    }

    @Test
    @DisplayName("should_NormalizeEmail_When_Registering")
    void should_NormalizeEmail_When_Registering() {
        RegisterUserRequest upperCaseEmailRequest = new RegisterUserRequest(
                "TEST@EXAMPLE.COM", "Test User", "SecurePass123!", "John",
                "Doe", null, null,
                java.util.UUID
                        .fromString("10000000-0000-0000-0000-000000000001"),
                java.util.UUID
                        .fromString("20000000-0000-0000-0000-000000000001"));

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = registerUserUseCase
                .execute(upperCaseEmailRequest);

        assertEquals("test@example.com", response.email());
    }

}
