package com.cashly.cashly_api.cash_register.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.cash_register.application.ports.in.DebitNoteCashApi;
import com.cashly.cashly_api.cash_register.application.services.CashMovementService;
import com.cashly.cashly_api.cash_register.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cash_register.domain.valueobjects.CashMovementType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class LocalDebitNoteCashApi implements DebitNoteCashApi {
    private final CashMovementService movements;

    public LocalDebitNoteCashApi(CashMovementService movements) {
        this.movements = movements;
    }

    @Override
    public void recordDebitNoteIn(BigDecimal amount, String noteNumber, UUID noteId,
                                  UUID authenticatedUserId, String createdByName) {
        movements.record(CashMovementDirection.IN, CashMovementType.DEBIT_NOTE, amount,
                null, "Nota de débito " + noteNumber, "DEBIT_NOTE", noteId,
                authenticatedUserId, createdByName);
    }
}
