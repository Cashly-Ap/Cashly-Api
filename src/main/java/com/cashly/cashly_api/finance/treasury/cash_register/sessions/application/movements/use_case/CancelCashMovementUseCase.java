package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.use_case;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.request.CancelCashMovementRequest;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.response.CashMovementResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.out.CashRegisterActorGateway;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.repository.CashRegisterRepository;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.RegisterStatus;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class CancelCashMovementUseCase {

    private final CashRegisterRepository registerRepository;
    private final CashRegisterActorGateway actors;

    public CancelCashMovementUseCase(
            CashRegisterRepository registerRepository,
            CashRegisterActorGateway actors) {
        this.registerRepository = registerRepository;
        this.actors = actors;
    }

    @Transactional
    @Audited(module = "finance", entityType = "CASH_MOVEMENT", action = "CANCEL")
    public CashMovementResponse execute(UUID movementId,
            CancelCashMovementRequest request, UUID userId) {
        var movement = registerRepository.findMovementById(movementId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cash movement not found: " + movementId));

        if (movement.getType() != CashMovementType.EXPENSE
                || !"MANUAL".equals(movement.getSourceType())) {
            throw new IllegalStateException(
                    "Only manually registered expenses can be cancelled");
        }

        var register = registerRepository.findById(movement.getRegisterId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cash register not found for movement: " + movementId));

        if (register.getStatus() != RegisterStatus.CLOSED) {
            throw new IllegalStateException(
                    "Expenses can only be cancelled from a closed cash register");
        }

        String userName = actors.findById(userId)
                .map(CashRegisterActorGateway.Actor::name)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found: " + userId));

        movement.cancel(request.reason(), userId, userName);
        var saved = registerRepository.saveMovement(movement);

        var adjustedExpense = register.getTotalExpense()
                .subtract(movement.getAmount()).max(java.math.BigDecimal.ZERO);
        register.applyMovementTotals(register.getTotalIncome(),
                adjustedExpense);
        registerRepository.save(register);

        return CashMovementResponse.from(saved);
    }
}
