package com.parsun.parsun_api.auth.user.application.usecases;

import com.parsun.parsun_api.auth.user.application.dto.request.UpdateUserModulePermissionsRequest;
import com.parsun.parsun_api.auth.user.application.dto.response.UserResponse;
import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.BusinessModule;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Password;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateUserModulePermissionsUseCase Tests")
class UpdateUserModulePermissionsUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateUserModulePermissionsUseCase useCase;

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
            Password.fromHash("$2a$10$hashedPassword")
        );
    }

    @Test
    @DisplayName("should_UpdatePermissions_When_UserExists")
    void should_UpdatePermissions_When_UserExists() {
        Set<BusinessModule> permissions = Set.of(BusinessModule.USERS, BusinessModule.INVENTORY);
        UpdateUserModulePermissionsRequest request = new UpdateUserModulePermissionsRequest(permissions);

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = useCase.execute(userId, request);

        assertNotNull(response);
        verify(userRepository).findById(any(UserId.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("should_ThrowIllegalArgumentException_When_UserDoesNotExist")
    void should_ThrowIllegalArgumentException_When_UserDoesNotExist() {
        Set<BusinessModule> permissions = Set.of(BusinessModule.USERS);
        UpdateUserModulePermissionsRequest request = new UpdateUserModulePermissionsRequest(permissions);

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute(userId, request)
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findById(any(UserId.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should_SaveUser_WithUpdatedPermissions")
    void should_SaveUser_WithUpdatedPermissions() {
        Set<BusinessModule> permissions = Set.of(BusinessModule.SALES, BusinessModule.FINANCE);
        UpdateUserModulePermissionsRequest request = new UpdateUserModulePermissionsRequest(permissions);

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(userId, request);

        verify(userRepository, times(1)).save(any(User.class));
    }
}
