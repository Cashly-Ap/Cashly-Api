package com.cashly.cashly_api.cash_register.application.services;
import com.cashly.cashly_api.cash_register.application.ports.in.CashRegisterOperations;
import com.cashly.cashly_api.cash_register.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cash_register.domain.valueobjects.CashMovementType;
import org.springframework.transaction.annotation.Transactional;
public class CashRegisterOperationsService implements CashRegisterOperations {
    private final CashMovementService movements;
    public CashRegisterOperationsService(CashMovementService movements) { this.movements = movements; }
    public void requireOpenRegister() { movements.requireOpenRegister(); }
    @Transactional
    public void record(Entry entry) {
        movements.record(CashMovementDirection.valueOf(entry.direction().name()), CashMovementType.valueOf(entry.type().name()),
                entry.amount(), entry.paymentMethodId(), entry.description(), entry.sourceType(), entry.sourceId(), entry.actorId(), entry.actorName());
    }
}
