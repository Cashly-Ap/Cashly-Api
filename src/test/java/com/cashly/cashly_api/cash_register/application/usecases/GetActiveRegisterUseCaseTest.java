package com.cashly.cashly_api.cash_register.application.usecases;

import com.cashly.cashly_api.cash_register.application.dto.CashRegisterResponse;
import com.cashly.cashly_api.cash_register.application.dto.DailyTotals;
import com.cashly.cashly_api.cash_register.domain.entities.CashRegister;
import com.cashly.cashly_api.cash_register.application.ports.out.CashRegisterQueryGateway;
import com.cashly.cashly_api.cash_register.domain.repositories.CashRegisterRepository;
import com.cashly.cashly_api.cash_register.domain.valueobjects.RegisterId;
import com.cashly.cashly_api.cash_register.domain.valueobjects.RegisterStatus;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetActiveRegisterUseCase Unit Tests")
class GetActiveRegisterUseCaseTest {

    @Mock private CashRegisterQueryGateway queries;

    @Mock
    private CashRegisterRepository closureRepository;

    @Mock
    private GetActiveRegisterUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GetActiveRegisterUseCase(closureRepository, queries);
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
    void should_ReturnActiveRegisterAcrossCalendarDays() {
        CashRegister activeClosure = CashRegister.reconstitute(
                RegisterId.generate(), LocalDate.now().minusDays(2), RegisterStatus.OPEN,
                userId, "Test User", LocalDateTime.now().minusDays(2),
                null, null, null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, 0, 0, 0, 0,
                BigDecimal.ZERO, null, null, null, 0,
                LocalDateTime.now().minusDays(2), LocalDateTime.now(), List.of());
        when(closureRepository.findActiveClosure()).thenReturn(Optional.of(activeClosure));
        when(queries.findSessionTotals(activeClosure.getId())).thenReturn(emptyDailyTotals());
        when(closureRepository.save(any(CashRegister.class))).thenReturn(activeClosure);

        Optional<CashRegisterResponse> response = useCase.execute();

        assertTrue(response.isPresent());
        assertEquals(activeClosure.getId().value(), response.orElseThrow().id());
        verify(closureRepository).save(any(CashRegister.class));
    }

    @Test
    void should_ReturnEmptyWithoutCreatingRegister_When_NoActiveRegisterExists() {
        when(closureRepository.findActiveClosure()).thenReturn(Optional.empty());

        Optional<CashRegisterResponse> response = useCase.execute();

        assertTrue(response.isEmpty());
        verify(closureRepository, never()).save(any());
    }
}
