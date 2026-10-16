package com.cashly.cashly_api.debit_notes.infrastructure.integration;

import com.cashly.cashly_api.cash_register.sessions.application.ports.in.DebitNoteCashApi;
import com.cashly.cashly_api.debit_notes.application.ports.out.DebitNoteCashGateway;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class LocalDebitNoteCashGateway implements DebitNoteCashGateway {
    private final DebitNoteCashApi cash;

    public LocalDebitNoteCashGateway(DebitNoteCashApi cash) {
        this.cash = cash;
    }

    @Override
    public void recordCashIn(BigDecimal amount, String noteNumber, UUID noteId,
                             UUID authenticatedUserId, String createdByName) {
        cash.recordDebitNoteIn(amount, noteNumber, noteId, authenticatedUserId, createdByName);
    }
}
