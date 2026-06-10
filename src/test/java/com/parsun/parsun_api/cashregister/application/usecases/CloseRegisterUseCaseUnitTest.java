package com.parsun.parsun_api.cashregister.application.usecases;

import com.parsun.parsun_api.cashregister.application.dto.DailyTotals;
import com.parsun.parsun_api.cashregister.domain.entities.CashRegister;
import com.parsun.parsun_api.cashregister.domain.repositories.CashRegisterRepository;
import com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterId;
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

    @Mock
    private CashRegisterRepository closureRepository;

    private CloseRegisterUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID closureId = UUID.randomUUID();
    private CashRegister openClosure;

    @BeforeEach
    void setUp() {
        useCase = new CloseRegisterUseCase(closureRepository);
        openClosure = CashRegister.open(RegisterId.of(closureId), LocalDate.now(), userId, "Operator");
    }

    @Test
    void should_CloseClosure_When_StatusIsOpen() {
        when(closureRepository.findById(RegisterId.of(closureId))).thenReturn(Optional.of(openClosure));
        when(closureRepository.findDailyTotals(any(LocalDate.class)))
                .thenReturn(new DailyTotals(
                        BigDecimal.ZERO, 0, BigDecimal.ZERO, 0,
                        BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, Collections.emptyList()));
        when(closureRepository.save(any(CashRegister.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID closedBy = UUID.randomUUID();
        var response = useCase.execute(closureId, closedBy, "Supervisor", "Cierre normal");

        assertNotNull(response);
        assertEquals("CLOSED", response.status().name());
        assertEquals(closedBy, response.closedBy());
        assertEquals("Supervisor", response.closedByName());
        assertEquals("Cierre normal", response.closingNotes());
    }

    @Test
    void should_ThrowException_When_ClosureNotFound() {
        when(closureRepository.findById(RegisterId.of(closureId))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(closureId, UUID.randomUUID(), "Test", null));
    }
}
