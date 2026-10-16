package com.cashly.cashly_api.customers.balance.application.ports.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface CreditNoteBalanceApi {
    void addCredit(UUID customerId, BigDecimal amount, UUID noteId, String noteNumber,
                   String invoiceNumber, UUID authenticatedUserId);
}
