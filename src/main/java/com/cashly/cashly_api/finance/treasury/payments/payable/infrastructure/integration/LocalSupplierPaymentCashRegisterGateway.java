package com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.integration;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.services.CashMovementService;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.repository.CashRegisterRepository;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.ports.out.SupplierPaymentCashRegisterGateway;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Component
public class LocalSupplierPaymentCashRegisterGateway
        implements SupplierPaymentCashRegisterGateway {

    private final CashMovementService movements;
    private final CashRegisterRepository registers;

    public LocalSupplierPaymentCashRegisterGateway(
            CashMovementService movements, CashRegisterRepository registers) {
        this.movements = movements;
        this.registers = registers;
    }

    @Override
    public void recordOutgoing(LocalDate date, BigDecimal amount,
            UUID paymentMethodId, String description, UUID sourceId,
            UUID actorId, String actorName) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        var register = registers.findByClosureDate(effectiveDate);
        if (register.isPresent()) {
            movements.recordForRegister(register.get().getId(),
                    CashMovementDirection.OUT, CashMovementType.EXPENSE,
                    amount, paymentMethodId, description, "SUPPLIER_PAYMENT",
                    sourceId, actorId, actorName,
                    effectiveDate.atTime(LocalTime.NOON));
            return;
        }
        movements.requireOpenRegister();
        movements.record(CashMovementDirection.OUT, CashMovementType.EXPENSE,
                amount, paymentMethodId, description, "SUPPLIER_PAYMENT",
                sourceId, actorId, actorName);
    }
}
