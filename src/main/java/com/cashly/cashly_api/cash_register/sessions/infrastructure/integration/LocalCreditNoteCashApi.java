package com.cashly.cashly_api.cash_register.sessions.infrastructure.integration;

import com.cashly.cashly_api.cash_register.sessions.application.ports.in.CreditNoteCashApi;
import com.cashly.cashly_api.cash_register.sessions.application.movements.services.CashMovementService;
import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.CashMovementType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class LocalCreditNoteCashApi implements CreditNoteCashApi {
    private final CashMovementService movements;

    public LocalCreditNoteCashApi(CashMovementService movements) {
        this.movements = movements;
    }

    @Override
    public void recordCreditNoteOut(BigDecimal amount, String noteNumber, UUID noteId,
                                    UUID authenticatedUserId, String createdByName) {
        movements.record(CashMovementDirection.OUT, CashMovementType.CREDIT_NOTE, amount,
                null, "Nota de crédito " + noteNumber, "CREDIT_NOTE", noteId,
                authenticatedUserId, createdByName);
    }
}
