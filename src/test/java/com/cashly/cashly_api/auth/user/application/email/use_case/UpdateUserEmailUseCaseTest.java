package com.cashly.cashly_api.auth.user.application.email.use_case;

import com.cashly.cashly_api.auth.user.application.email.request.UpdateUserEmailRequest;
import com.cashly.cashly_api.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.auth.user.application.exceptions.DuplicateEmailException;
import com.cashly.cashly_api.auth.user.application.exceptions.UserNotFoundException;
import com.cashly.cashly_api.auth.user.domain.entity.User;
import com.cashly.cashly_api.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Password;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserName;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateUserEmailUseCase Tests")
class UpdateUserEmailUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateUserEmailUseCase useCase;

    private User testUser;
    private String userId;

    @BeforeEach
    void setUp() {
        UUID uuid = UUID.randomUUID();
        userId = uuid.toString();
        testUser = new User(
            new UserId(uuid),
            new Email("old@example.com"),
            new UserName("Test User"),
            Password.fromHash("$2a$10$hashedPassword")
        );
    }

    @Test
    @DisplayName("should_UpdateEmail_When_UserExistsAndEmailIsUnique")
    void should_UpdateEmail_When_UserExistsAndEmailIsUnique() {
        UpdateUserEmailRequest request = new UpdateUserEmailRequest("new@example.com");

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = useCase.execute(userId, request);

        assertNotNull(response);
        assertEquals("new@example.com", response.email());
        verify(userRepository).findById(any(UserId.class));
        verify(userRepository).existsByEmail(any(Email.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("should_ThrowUserNotFoundException_When_UserDoesNotExist")
    void should_ThrowUserNotFoundException_When_UserDoesNotExist() {
        UpdateUserEmailRequest request = new UpdateUserEmailRequest("new@example.com");

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> useCase.execute(userId, request)
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findById(any(UserId.class));
        verify(userRepository, never()).existsByEmail(any(Email.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should_ThrowDuplicateEmailException_When_EmailAlreadyInUse")
    void should_ThrowDuplicateEmailException_When_EmailAlreadyInUse() {
        UpdateUserEmailRequest request = new UpdateUserEmailRequest("taken@example.com");

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

        DuplicateEmailException exception = assertThrows(
            DuplicateEmailException.class,
            () -> useCase.execute(userId, request)
        );

        assertTrue(exception.getMessage().contains("Email already in use"));
        verify(userRepository).findById(any(UserId.class));
        verify(userRepository).existsByEmail(any(Email.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should_AllowSameEmail_When_UserKeepsCurrentEmail")
    void should_AllowSameEmail_When_UserKeepsCurrentEmail() {
        UpdateUserEmailRequest request = new UpdateUserEmailRequest("old@example.com");

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = useCase.execute(userId, request);

        assertNotNull(response);
        assertEquals("old@example.com", response.email());
        verify(userRepository).save(any(User.class));
    }
}
