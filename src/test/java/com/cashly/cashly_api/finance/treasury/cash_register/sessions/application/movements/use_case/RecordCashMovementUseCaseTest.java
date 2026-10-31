package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.use_case;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.out.CashRegisterActorGateway;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.request.CashMovementRequest;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.services.CashMovementService;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashMovement;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashRegister;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.repository.CashRegisterRepository;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.RegisterId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecordCashMovementUseCaseTest {
    private final CashRegisterRepository registers = mock(
            CashRegisterRepository.class);
    private final CashRegisterActorGateway actors = mock(
            CashRegisterActorGateway.class);
    private final CashMovementService movements = mock(
            CashMovementService.class);
    private final RecordCashMovementUseCase useCase = new RecordCashMovementUseCase(
            registers, actors, movements);

    @Test
    void shouldRecordMovementWithActorNameFromCashRegisterContract() {
        UUID registerId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        var register = CashRegister.open(RegisterId.of(registerId),
                LocalDate.now(), actorId, "Opener", BigDecimal.ZERO);
        var request = new CashMovementRequest(CashMovementDirection.IN,
                CashMovementType.PAYMENT, BigDecimal.TEN, null,
                "Manual income");
        when(registers.findActiveClosure()).thenReturn(Optional.of(register));
        when(actors.findById(actorId)).thenReturn(Optional
                .of(new CashRegisterActorGateway.Actor(actorId, "Operator")));
        CashMovement movement = CashMovement.create(register.getId(),
                request.direction(), request.type(), request.amount(), null,
                "Sin método", request.description(), "MANUAL", null, actorId,
                "Operator");
        when(movements.record(request.direction(), request.type(),
                request.amount(), null, request.description(), "MANUAL", null,
                actorId, "Operator")).thenReturn(movement);

        var response = useCase.execute(registerId, request, actorId);

        assertThat(response.createdByName()).isEqualTo("Operator");
        assertThat(response.paymentMethodName()).isEqualTo("Sin método");
        verify(movements).record(request.direction(), request.type(),
                request.amount(), null, request.description(), "MANUAL", null,
                actorId, "Operator");
    }

    @Test
    void shouldRejectUnknownActorBeforeRecordingMovement() {
        UUID registerId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        var register = CashRegister.open(RegisterId.of(registerId),
                LocalDate.now(), actorId, "Opener", BigDecimal.ZERO);
        when(registers.findActiveClosure()).thenReturn(Optional.of(register));
        when(actors.findById(actorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(registerId,
                new CashMovementRequest(CashMovementDirection.IN,
                        CashMovementType.PAYMENT, BigDecimal.TEN, null,
                        "Manual income"),
                actorId)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found: " + actorId);
        verify(movements, never()).record(any(), any(), any(), any(), any(),
                any(), any(), any(), any());
    }
}
