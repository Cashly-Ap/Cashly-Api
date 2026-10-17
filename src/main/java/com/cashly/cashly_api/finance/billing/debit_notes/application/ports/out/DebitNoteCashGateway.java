package com.cashly.cashly_api.finance.billing.debit_notes.application.ports.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface DebitNoteCashGateway {
    void recordCashIn(BigDecimal amount, String noteNumber, UUID noteId,
                      UUID authenticatedUserId, String createdByName);
}
