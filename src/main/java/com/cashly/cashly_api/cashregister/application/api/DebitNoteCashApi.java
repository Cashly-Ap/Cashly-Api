package com.cashly.cashly_api.cashregister.application.api;

import java.math.BigDecimal;
import java.util.UUID;

public interface DebitNoteCashApi {
    void recordDebitNoteIn(BigDecimal amount, String noteNumber, UUID noteId,
                           UUID authenticatedUserId, String createdByName);
}
