package com.cashly.cashly_api.cash_register.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.cash_register.application.ports.in.CreditNoteCashApi;
import com.cashly.cashly_api.cash_register.application.services.CashMovementService;
import com.cashly.cashly_api.cash_register.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cash_register.domain.valueobjects.CashMovementType;
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
