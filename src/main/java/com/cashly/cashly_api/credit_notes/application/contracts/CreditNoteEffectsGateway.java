package com.cashly.cashly_api.credit_notes.application.contracts;

import java.math.BigDecimal;
import java.util.UUID;

public interface CreditNoteEffectsGateway {
    void recordCashOut(BigDecimal amount, String noteNumber, UUID noteId,
                       UUID authenticatedUserId, String createdByName);
    void addCustomerCredit(UUID customerId, BigDecimal amount, UUID noteId,
                           String noteNumber, String invoiceNumber, UUID authenticatedUserId);
}
