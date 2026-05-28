package com.parsun.parsun_api.returns.domain.services;

import java.math.BigDecimal;
import java.util.UUID;

public interface CreditNoteCreator {

    void createCreditNote(UUID customerId, UUID invoiceId, BigDecimal amount,
                          String reason, UUID createdBy, String createdByName);
}
