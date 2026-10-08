package com.cashly.cashly_api.cashregister.application.usecases;

import com.cashly.cashly_api.cashregister.application.contracts.CashRegisterActorGateway;
import com.cashly.cashly_api.cashregister.application.dto.CashMovementRequest;
import com.cashly.cashly_api.cashregister.application.services.CashMovementService;
import com.cashly.cashly_api.cashregister.domain.entities.CashMovement;
import com.cashly.cashly_api.cashregister.domain.entities.CashRegister;
import com.cashly.cashly_api.cashregister.domain.repositories.CashRegisterRepository;
import com.cashly.cashly_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cashregister.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.cashregister.domain.valueobjects.RegisterId;
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
    private final CashRegisterRepository registers = mock(CashRegisterRepository.class);
    private final CashRegisterActorGateway actors = mock(CashRegisterActorGateway.class);
    private final CashMovementService movements = mock(CashMovementService.class);
    private final RecordCashMovementUseCase useCase = new RecordCashMovementUseCase(registers, actors, movements);

    @Test
    void shouldRecordMovementWithActorNameFromCashRegisterContract() {
        UUID registerId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        var register = CashRegister.open(RegisterId.of(registerId), LocalDate.now(), actorId, "Opener", BigDecimal.ZERO);
        var request = new CashMovementRequest(CashMovementDirection.IN, CashMovementType.PAYMENT, BigDecimal.TEN,
                null, "Manual income");
        when(registers.findActiveClosure()).thenReturn(Optional.of(register));
        when(actors.findById(actorId)).thenReturn(Optional.of(new CashRegisterActorGateway.Actor(actorId, "Operator")));
        CashMovement movement = CashMovement.create(register.getId(), request.direction(), request.type(), request.amount(),
                null, "Sin método", request.description(), "MANUAL", null, actorId, "Operator");
        when(movements.record(request.direction(), request.type(), request.amount(), null, request.description(),
                "MANUAL", null, actorId, "Operator")).thenReturn(movement);

        var response = useCase.execute(registerId, request, actorId);

        assertThat(response.createdByName()).isEqualTo("Operator");
        assertThat(response.paymentMethodName()).isEqualTo("Sin método");
        verify(movements).record(request.direction(), request.type(), request.amount(), null,
                request.description(), "MANUAL", null, actorId, "Operator");
    }

    @Test
    void shouldRejectUnknownActorBeforeRecordingMovement() {
        UUID registerId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        var register = CashRegister.open(RegisterId.of(registerId), LocalDate.now(), actorId, "Opener", BigDecimal.ZERO);
        when(registers.findActiveClosure()).thenReturn(Optional.of(register));
        when(actors.findById(actorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(registerId,
                new CashMovementRequest(CashMovementDirection.IN, CashMovementType.PAYMENT,
                        BigDecimal.TEN, null, "Manual income"), actorId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found: " + actorId);
        verify(movements, never()).record(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }
}
