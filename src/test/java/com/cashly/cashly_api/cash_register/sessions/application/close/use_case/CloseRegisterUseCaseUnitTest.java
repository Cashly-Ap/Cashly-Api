package com.cashly.cashly_api.cash_register.sessions.application.close.use_case;

import com.cashly.cashly_api.cash_register.sessions.application.dto.response.DailyTotals;
import com.cashly.cashly_api.cash_register.sessions.domain.entity.CashRegister;
import com.cashly.cashly_api.cash_register.application.ports.out.CashRegisterQueryGateway;
import com.cashly.cashly_api.cash_register.sessions.domain.repository.CashRegisterRepository;
import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.RegisterId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CloseRegisterUseCase Unit Tests")
class CloseRegisterUseCaseUnitTest {

    @Mock private CashRegisterQueryGateway queries;

    @Mock
    private CashRegisterRepository closureRepository;

    private CloseRegisterUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID closureId = UUID.randomUUID();
    private CashRegister openClosure;

    @BeforeEach
    void setUp() {
        useCase = new CloseRegisterUseCase(closureRepository, queries);
        openClosure = CashRegister.open(RegisterId.of(closureId), LocalDate.now(), userId, "Operator");
    }

    @Test
    void should_CloseClosure_When_StatusIsOpen() {
        when(closureRepository.findById(RegisterId.of(closureId))).thenReturn(Optional.of(openClosure));
        when(queries.findSessionTotals(any(RegisterId.class)))
                .thenReturn(new DailyTotals(
                        BigDecimal.ZERO, 0, BigDecimal.ZERO, 0,
                        BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, Collections.emptyList()));
        when(closureRepository.save(any(CashRegister.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID closedBy = UUID.randomUUID();
        var response = useCase.execute(closureId, closedBy, "Supervisor",
                new BigDecimal("95.00"), "Cierre normal");

        assertNotNull(response);
        assertEquals("CLOSED", response.status().name());
        assertEquals(closedBy, response.closedBy());
        assertEquals("Supervisor", response.closedByName());
        assertEquals("Cierre normal", response.closingNotes());
        assertEquals(new BigDecimal("95.00"), response.declaredBalance());
        assertEquals(new BigDecimal("95.00"), response.difference());
    }

    @Test
    void should_ThrowException_When_ClosureNotFound() {
        when(closureRepository.findById(RegisterId.of(closureId))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(closureId, UUID.randomUUID(), "Test", BigDecimal.ZERO, null));
    }
}
