package com.parsun.parsun_api.cashregister.application.usecases;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserName;
import com.parsun.parsun_api.cashregister.application.dto.CashRegisterResponse;
import com.parsun.parsun_api.cashregister.application.dto.DailyTotals;
import com.parsun.parsun_api.cashregister.domain.entities.CashRegister;
import com.parsun.parsun_api.cashregister.domain.repositories.CashRegisterRepository;
import com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterId;
import com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetActiveRegisterUseCase Unit Tests")
class GetActiveRegisterUseCaseTest {

    @Mock
    private CashRegisterRepository closureRepository;

    @Mock
    private UserRepository userRepository;

    private GetActiveRegisterUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GetActiveRegisterUseCase(closureRepository, userRepository);
    }

    private DailyTotals emptyDailyTotals() {
        return new DailyTotals(
                BigDecimal.ZERO, 0, BigDecimal.ZERO, 0,
                BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, List.of()
        );
    }

    private CashRegister openRegister() {
        return CashRegister.reconstitute(
                RegisterId.generate(), LocalDate.now(), RegisterStatus.OPEN,
                userId, "Test User", LocalDateTime.now(),
                null, null, null,
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO,
                0, 0, 0, 0,
                BigDecimal.ZERO, null, null, null,
                0, LocalDateTime.now(), LocalDateTime.now(),
                List.of()
        );
    }

    @Test
    void should_ReturnActiveClosure_When_ActiveClosureExists() {
        CashRegister activeClosure = openRegister();
        when(closureRepository.findActiveClosure()).thenReturn(Optional.of(activeClosure));
        when(closureRepository.findDailyTotals(activeClosure.getClosureDate())).thenReturn(emptyDailyTotals());
        when(closureRepository.save(any(CashRegister.class))).thenReturn(activeClosure);

        CashRegisterResponse response = useCase.execute(userId);

        assertNotNull(response);
        verify(closureRepository).save(any(CashRegister.class));
    }

    @Test
    void should_ReturnTodayClosure_When_NoActiveClosureButTodayClosureExists() {
        CashRegister todayClosure = openRegister();
        when(closureRepository.findActiveClosure()).thenReturn(Optional.empty());
        when(closureRepository.findByClosureDate(LocalDate.now())).thenReturn(Optional.of(todayClosure));
        when(closureRepository.findDailyTotals(todayClosure.getClosureDate())).thenReturn(emptyDailyTotals());

        CashRegisterResponse response = useCase.execute(userId);

        assertNotNull(response);
    }

    @Test
    void should_OpenNewRegister_When_NoActiveOrTodayClosureExists() {
        User user = mock(User.class);
        when(user.getUserName()).thenReturn(new UserName("Test User"));
        when(closureRepository.findActiveClosure()).thenReturn(Optional.empty());
        when(closureRepository.findByClosureDate(LocalDate.now())).thenReturn(Optional.empty());
        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(user));
        when(closureRepository.save(any(CashRegister.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(closureRepository.findDailyTotals(any(LocalDate.class))).thenReturn(emptyDailyTotals());

        CashRegisterResponse response = useCase.execute(userId);

        assertNotNull(response);
        verify(closureRepository).save(any(CashRegister.class));
    }

    @Test
    void should_Throw_When_UserNotFoundAndNoClosures() {
        when(closureRepository.findActiveClosure()).thenReturn(Optional.empty());
        when(closureRepository.findByClosureDate(LocalDate.now())).thenReturn(Optional.empty());
        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> useCase.execute(userId));
    }
}
