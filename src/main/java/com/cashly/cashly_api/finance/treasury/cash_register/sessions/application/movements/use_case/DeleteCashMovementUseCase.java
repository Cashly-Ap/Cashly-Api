package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.use_case;

import com.cashly.cashly_api.finance.treasury.cash_register.application.ports.out.CashRegisterQueryGateway;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.repository.CashRegisterRepository;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.RegisterStatus;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class DeleteCashMovementUseCase {

    private final CashRegisterRepository registerRepository;
    private final CashRegisterQueryGateway queries;

    public DeleteCashMovementUseCase(CashRegisterRepository registerRepository,
            CashRegisterQueryGateway queries) {
        this.registerRepository = registerRepository;
        this.queries = queries;
    }

    @Transactional
    @Audited(module = "finance", entityType = "CASH_MOVEMENT", action = "DELETE")
    public void execute(UUID movementId) {
        var movement = registerRepository.findMovementById(movementId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cash movement not found: " + movementId));

        if (movement.getType() != CashMovementType.EXPENSE
                || !"MANUAL".equals(movement.getSourceType())) {
            throw new IllegalStateException(
                    "Only manually registered expenses can be deleted");
        }

        var register = registerRepository.findById(movement.getRegisterId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cash register not found for movement: " + movementId));

        if (register.getStatus() != RegisterStatus.OPEN) {
            throw new IllegalStateException(
                    "Expenses can only be deleted from an open cash register");
        }

        registerRepository.deleteMovement(movementId);

        var totals = queries.findSessionTotals(register.getId());
        register.applyMovementTotals(totals.totalRevenue(),
                totals.totalExpenses());
        registerRepository.save(register);
    }
}
