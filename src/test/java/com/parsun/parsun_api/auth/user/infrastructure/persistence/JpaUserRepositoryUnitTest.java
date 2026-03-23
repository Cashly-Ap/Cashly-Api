package com.parsun.parsun_api.auth.user.infrastructure.persistence;

import com.parsun.parsun_api.auth.user.infrastructure.persistence.entities.UserEntity;
import com.parsun.parsun_api.auth.user.infrastructure.persistence.repositories.JpaUserRepository;
import com.parsun.parsun_api.auth.user.infrastructure.persistence.repositories.SpringDataUserRepository;
import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserName;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Password;
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
@DisplayName("JpaUserRepository Unit Tests")
class JpaUserRepositoryUnitTest {

    @Mock
    private SpringDataUserRepository springDataUserRepository;

    @InjectMocks
    private JpaUserRepository jpaUserRepository;

    private User testUser;
    private UserEntity testUserEntity;

    @BeforeEach
    void setUp() {
        UserId userId = UserId.generate();
        Email email = new Email("test@example.com");
        Password password = Password.fromHash("$2a$10$hashedPassword");

        testUser = new User(userId, email, new UserName("Test User"), password);

        testUserEntity = UserEntity.fromDomain(testUser);
    }

    @Test
    @DisplayName("should_SaveUser_When_NewUserProvided")
    void should_SaveUser_When_NewUserProvided() {
        when(springDataUserRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        when(springDataUserRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        User savedUser = jpaUserRepository.save(testUser);

        assertNotNull(savedUser);
        assertEquals(testUser.getId(), savedUser.getId());
        assertEquals(testUser.getEmail(), savedUser.getEmail());
        verify(springDataUserRepository, times(1)).findById(any(UUID.class));
        verify(springDataUserRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("should_UpdateUser_When_ExistingUserProvided")
    void should_UpdateUser_When_ExistingUserProvided() {
        when(springDataUserRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUserEntity));
        when(springDataUserRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        User savedUser = jpaUserRepository.save(testUser);

        assertNotNull(savedUser);
        verify(springDataUserRepository, times(1)).findById(any(UUID.class));
        verify(springDataUserRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("should_ThrowException_When_SavingNullUser")
    void should_ThrowException_When_SavingNullUser() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> jpaUserRepository.save(null)
        );

        assertEquals("User cannot be null", exception.getMessage());
        verify(springDataUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("should_FindUserById_When_UserExists")
    void should_FindUserById_When_UserExists() {
        UserId userId = testUser.getId();
        when(springDataUserRepository.findById(userId.getValue()))
            .thenReturn(Optional.of(testUserEntity));

        Optional<User> foundUser = jpaUserRepository.findById(userId);

        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getId(), foundUser.get().getId());
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
        verify(springDataUserRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    @DisplayName("should_ReturnEmpty_When_UserNotFoundById")
    void should_ReturnEmpty_When_UserNotFoundById() {
        UserId userId = UserId.generate();
        when(springDataUserRepository.findById(userId.getValue()))
            .thenReturn(Optional.empty());

        Optional<User> foundUser = jpaUserRepository.findById(userId);

        assertFalse(foundUser.isPresent());
        verify(springDataUserRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    @DisplayName("should_ThrowException_When_FindingByNullId")
    void should_ThrowException_When_FindingByNullId() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> jpaUserRepository.findById(null)
        );

        assertEquals("User ID cannot be null", exception.getMessage());
        verify(springDataUserRepository, never()).findById(any(UUID.class));
    }

    @Test
    @DisplayName("should_FindUserByEmail_When_UserExists")
    void should_FindUserByEmail_When_UserExists() {
        Email email = testUser.getEmail();
        when(springDataUserRepository.findByEmail(email.getValue()))
            .thenReturn(Optional.of(testUserEntity));

        Optional<User> foundUser = jpaUserRepository.findByEmail(email);

        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
        verify(springDataUserRepository, times(1)).findByEmail(anyString());
    }

    @Test
    @DisplayName("should_ReturnEmpty_When_UserNotFoundByEmail")
    void should_ReturnEmpty_When_UserNotFoundByEmail() {
        Email email = new Email("nonexistent@example.com");
        when(springDataUserRepository.findByEmail(email.getValue()))
            .thenReturn(Optional.empty());

        Optional<User> foundUser = jpaUserRepository.findByEmail(email);

        assertFalse(foundUser.isPresent());
        verify(springDataUserRepository, times(1)).findByEmail(anyString());
    }

    @Test
    @DisplayName("should_ThrowException_When_FindingByNullEmail")
    void should_ThrowException_When_FindingByNullEmail() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> jpaUserRepository.findByEmail(null)
        );

        assertEquals("Email cannot be null", exception.getMessage());
        verify(springDataUserRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("should_ReturnTrue_When_UserExistsByEmail")
    void should_ReturnTrue_When_UserExistsByEmail() {
        Email email = testUser.getEmail();
        when(springDataUserRepository.existsByEmail(email.getValue())).thenReturn(true);

        boolean exists = jpaUserRepository.existsByEmail(email);

        assertTrue(exists);
        verify(springDataUserRepository, times(1)).existsByEmail(anyString());
    }

    @Test
    @DisplayName("should_ReturnFalse_When_UserDoesNotExistByEmail")
    void should_ReturnFalse_When_UserDoesNotExistByEmail() {
        Email email = new Email("nonexistent@example.com");
        when(springDataUserRepository.existsByEmail(email.getValue())).thenReturn(false);

        boolean exists = jpaUserRepository.existsByEmail(email);

        assertFalse(exists);
        verify(springDataUserRepository, times(1)).existsByEmail(anyString());
    }

    @Test
    @DisplayName("should_ThrowException_When_CheckingExistenceWithNullEmail")
    void should_ThrowException_When_CheckingExistenceWithNullEmail() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> jpaUserRepository.existsByEmail(null)
        );

        assertEquals("Email cannot be null", exception.getMessage());
        verify(springDataUserRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("should_DeleteUser_When_ValidUserProvided")
    void should_DeleteUser_When_ValidUserProvided() {
        doNothing().when(springDataUserRepository).deleteById(any(UUID.class));

        jpaUserRepository.delete(testUser);

        verify(springDataUserRepository, times(1)).deleteById(testUser.getId().getValue());
    }

    @Test
    @DisplayName("should_ThrowException_When_DeletingNullUser")
    void should_ThrowException_When_DeletingNullUser() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> jpaUserRepository.delete(null)
        );

        assertEquals("User cannot be null", exception.getMessage());
        verify(springDataUserRepository, never()).deleteById(any(UUID.class));
    }
}
