package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.open.use_case;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashRegister;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.repository.CashRegisterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenRegisterUseCase Unit Tests")
class OpenRegisterUseCaseUnitTest {

    @Mock
    private CashRegisterRepository closureRepository;

    private OpenRegisterUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final String userName = "Operator Test";

    @BeforeEach
    void setUp() {
        useCase = new OpenRegisterUseCase(closureRepository);
    }

    @Test
    void should_OpenRegisterWithOpeningBalance_When_NoRegisterIsOpen() {
        when(closureRepository.findActiveClosure())
                .thenReturn(Optional.empty());
        when(closureRepository.save(any(CashRegister.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(userId, userName,
                new BigDecimal("125.50"));

        assertNotNull(response);
        assertEquals(userId, response.openedBy());
        assertEquals(userName, response.openedByName());
        assertEquals(LocalDate.now(), response.closureDate());
        assertEquals(new BigDecimal("125.50"), response.openingBalance());
        assertEquals(new BigDecimal("125.50"), response.expectedBalance());
        verify(closureRepository).findActiveClosure();
        verify(closureRepository).save(any(CashRegister.class));
    }

    @Test
    void should_RejectOpening_When_AnotherRegisterIsOpen() {
        when(closureRepository.findActiveClosure())
                .thenReturn(Optional.of(mock(CashRegister.class)));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> useCase.execute(userId, userName, BigDecimal.ZERO));

        assertTrue(exception.getMessage().contains("already open"));
        verify(closureRepository, never()).save(any());
    }

    @Test
    void should_AllowAnotherSessionOnSameDay_When_PreviousSessionIsClosed() {
        when(closureRepository.findActiveClosure())
                .thenReturn(Optional.empty());
        when(closureRepository.save(any(CashRegister.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(userId, userName, BigDecimal.ZERO);

        assertEquals("OPEN", response.status().name());
        verify(closureRepository, never()).findByClosureDate(any());
    }
}
