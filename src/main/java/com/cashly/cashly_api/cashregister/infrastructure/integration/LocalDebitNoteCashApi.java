package com.cashly.cashly_api.cashregister.infrastructure.integration;

import com.cashly.cashly_api.cashregister.application.api.DebitNoteCashApi;
import com.cashly.cashly_api.cashregister.application.services.CashMovementService;
import com.cashly.cashly_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cashregister.domain.valueobjects.CashMovementType;
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
