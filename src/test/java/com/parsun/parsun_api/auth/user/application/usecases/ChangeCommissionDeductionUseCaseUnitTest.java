package com.parsun.parsun_api.auth.user.application.usecases;

import com.parsun.parsun_api.auth.user.application.dto.request.ChangeCommissionDeductionRequest;
import com.parsun.parsun_api.auth.user.application.dto.response.UserResponse;
import com.parsun.parsun_api.auth.user.application.exceptions.UserNotFoundException;
import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
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

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChangeCommissionDeductionUseCase Unit Tests")
class ChangeCommissionDeductionUseCaseUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChangeCommissionDeductionUseCase changeCommissionDeductionUseCase;

    private User testUser;
    private UserId testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UserId.generate();
        testUser = new User(
            testUserId,
            new Email("test@example.com"),
            new UserName("Test User"),
            Password.fromHash("$2a$10$hashedPassword")
        );
    }

    @Test
    @DisplayName("should_UpdateCommissionDeduction_When_ValidRequest")
    void should_UpdateCommissionDeduction_When_ValidRequest() {
        ChangeCommissionDeductionRequest request = new ChangeCommissionDeductionRequest(
            new BigDecimal("5.00")
        );
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = changeCommissionDeductionUseCase.execute(testUserId.getValue().toString(), request);

        assertNotNull(response);
        verify(userRepository).findById(testUserId);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("should_ThrowUserNotFoundException_When_UserDoesNotExist")
    void should_ThrowUserNotFoundException_When_UserDoesNotExist() {
        ChangeCommissionDeductionRequest request = new ChangeCommissionDeductionRequest(
            new BigDecimal("3.00")
        );
        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
            () -> changeCommissionDeductionUseCase.execute(testUserId.getValue().toString(), request));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("should_UpdateToZero_When_PercentageIsZero")
    void should_UpdateToZero_When_PercentageIsZero() {
        ChangeCommissionDeductionRequest request = new ChangeCommissionDeductionRequest(
            BigDecimal.ZERO
        );
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = changeCommissionDeductionUseCase.execute(testUserId.getValue().toString(), request);

        assertNotNull(response);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("should_UpdateToMaximum_When_PercentageIs100")
    void should_UpdateToMaximum_When_PercentageIs100() {
        ChangeCommissionDeductionRequest request = new ChangeCommissionDeductionRequest(
            new BigDecimal("100.00")
        );
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = changeCommissionDeductionUseCase.execute(testUserId.getValue().toString(), request);

        assertNotNull(response);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("should_ReturnUpdatedUserResponse_When_ChangeSuccessful")
    void should_ReturnUpdatedUserResponse_When_ChangeSuccessful() {
        ChangeCommissionDeductionRequest request = new ChangeCommissionDeductionRequest(
            new BigDecimal("50.00")
        );
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = changeCommissionDeductionUseCase.execute(testUserId.getValue().toString(), request);

        assertNotNull(response);
        assertEquals(testUser.getEmail().getValue(), response.email());
        assertEquals(testUser.isActive(), response.active());
    }
}
