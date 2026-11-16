package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.use_case;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.request.CancelCashMovementRequest;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.out.CashRegisterActorGateway;
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

class CancelCashMovementUseCaseTest {

    private final CashRegisterRepository registers = mock(
            CashRegisterRepository.class);
    private final CashRegisterActorGateway actors = mock(
            CashRegisterActorGateway.class);
    private final CancelCashMovementUseCase useCase = new CancelCashMovementUseCase(
            registers, actors);

    @Test
    void shouldCancelExpenseFromClosedRegisterWithReason() {
        UUID movementId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID registerId = UUID.randomUUID();
        var register = closedRegisterWithExpense(registerId, userId);
        var movement = expense(register.getId(), userId);
        when(registers.findMovementById(movementId))
                .thenReturn(Optional.of(movement));
        when(registers.findById(register.getId()))
                .thenReturn(Optional.of(register));
        when(actors.findById(userId)).thenReturn(Optional
                .of(new CashRegisterActorGateway.Actor(userId, "Supervisor")));
        when(registers.saveMovement(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(movementId,
                new CancelCashMovementRequest("Duplicado"), userId);

        assertThat(response.cancelled()).isTrue();
        assertThat(response.cancellationReason()).isEqualTo("Duplicado");
        assertThat(response.cancelledByName()).isEqualTo("Supervisor");
        assertThat(register.getTotalExpense()).isEqualByComparingTo("0.00");
        verify(registers).saveMovement(movement);
        verify(registers).save(register);
    }

    @Test
    void shouldRejectCancellationWhenRegisterIsOpen() {
        UUID movementId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        var register = CashRegister.open(RegisterId.generate(),
                LocalDate.now(), userId, "Opener", BigDecimal.ZERO);
        var movement = expense(register.getId(), userId);
        when(registers.findMovementById(movementId))
                .thenReturn(Optional.of(movement));
        when(registers.findById(register.getId()))
                .thenReturn(Optional.of(register));

        assertThatThrownBy(() -> useCase.execute(movementId,
                new CancelCashMovementRequest("Duplicado"), userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Expenses can only be cancelled from a closed cash register");
        verify(registers, never()).saveMovement(any());
    }

    @Test
    void shouldRejectCancellationOfNonExpenseMovement() {
        UUID movementId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        var register = closedRegisterWithExpense(UUID.randomUUID(), userId);
        var movement = CashMovement.create(register.getId(),
                CashMovementDirection.IN, CashMovementType.PAYMENT,
                BigDecimal.TEN, null, "Efectivo", "Pago", "MANUAL", null,
                userId, "Operator");
        when(registers.findMovementById(movementId))
                .thenReturn(Optional.of(movement));

        assertThatThrownBy(() -> useCase.execute(movementId,
                new CancelCashMovementRequest("Duplicado"), userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only manually registered expenses can be cancelled");
    }

    private CashRegister closedRegisterWithExpense(UUID registerId,
            UUID userId) {
        var register = CashRegister.open(RegisterId.of(registerId),
                LocalDate.now(), userId, "Opener", BigDecimal.ZERO);
        register.applyMovementTotals(BigDecimal.ZERO, BigDecimal.TEN);
        register.close(userId, "Closer", BigDecimal.ZERO, "closed");
        return register;
    }

    private CashMovement expense(RegisterId registerId, UUID userId) {
        return CashMovement.create(registerId, CashMovementDirection.OUT,
                CashMovementType.EXPENSE, BigDecimal.TEN, null, "Efectivo",
                "Gasto", "MANUAL", null, userId, "Operator");
    }
}
