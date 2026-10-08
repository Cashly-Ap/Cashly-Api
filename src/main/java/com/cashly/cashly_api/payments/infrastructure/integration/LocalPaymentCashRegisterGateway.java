package com.cashly.cashly_api.payments.infrastructure.integration;
import com.cashly.cashly_api.payments.application.contracts.CashRegisterGateway;
import com.cashly.cashly_api.cashregister.application.services.CashMovementService;
import com.cashly.cashly_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cashregister.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.cashregister.application.api.CashRegisterOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public class LocalPaymentCashRegisterGateway implements CashRegisterGateway {
    private final CashMovementService movements;
    private final CashRegisterOperations operations;
    @Autowired
    public LocalPaymentCashRegisterGateway(CashMovementService movements) {
        this.movements = movements;
        this.operations = null;
    }
    public LocalPaymentCashRegisterGateway(CashRegisterOperations operations) {
        this.movements = null;
        this.operations = operations;
    }
    public void requireOpenRegister() {
        if (movements != null) movements.requireOpenRegister();
        else operations.requireOpenRegister();
    }
    public void recordMovement(Movement movement) {
        PaymentEntry entry = movement.payment();
        if (movements != null) {
            movements.record(movement.direction() == Direction.IN ? CashMovementDirection.IN : CashMovementDirection.OUT,
                    movement.type() == Type.REFUND ? CashMovementType.REFUND : CashMovementType.PAYMENT,
                    entry.amount(), entry.paymentMethodId(), entry.notes(), "PAYMENT", entry.paymentId(),
                    entry.actorId(), entry.actorName());
        } else {
            operations.record(new CashRegisterOperations.Entry(
                    movement.direction() == Direction.IN ? CashRegisterOperations.Direction.IN : CashRegisterOperations.Direction.OUT,
                    CashRegisterOperations.EntryType.PAYMENT, entry.amount(), entry.paymentMethodId(), entry.notes(),
                    "PAYMENT", entry.paymentId(), entry.actorId(), entry.actorName()));
        }
    }
    public void recordPayment(PaymentEntry entry) {
        recordMovement(new Movement(Direction.IN, Type.PAYMENT, entry));
    }
}
