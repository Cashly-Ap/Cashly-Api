package com.cashly.cashly_api.debit_notes.application.contracts;

import java.math.BigDecimal;
import java.util.UUID;

public interface DebitNoteCashGateway {
    void recordCashIn(BigDecimal amount, String noteNumber, UUID noteId,
                      UUID authenticatedUserId, String createdByName);
}
