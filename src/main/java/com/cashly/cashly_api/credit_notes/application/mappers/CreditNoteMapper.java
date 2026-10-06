package com.cashly.cashly_api.credit_notes.application.mappers;

import com.cashly.cashly_api.credit_notes.application.dto.response.CreditNoteResponse;
import com.cashly.cashly_api.credit_notes.domain.entities.CreditNote;

public class CreditNoteMapper {

    private CreditNoteMapper() {}

    public static CreditNoteResponse toCreditNoteResponse(CreditNote creditNote,
                                                           String customerName,
                                                           String customerRif,
                                                           String invoiceNumber) {
        return new CreditNoteResponse(
                creditNote.getId().value(),
                creditNote.getNoteNumber().value(),
                creditNote.getCustomerId(),
                customerName,
                customerRif,
                creditNote.getInvoiceId(),
                invoiceNumber,
                creditNote.getAmount(),
                creditNote.getReason(),
                creditNote.getCreatedBy(),
                creditNote.getCreatedByName(),
                creditNote.getCreatedAt(),
                creditNote.getUpdatedAt()
        );
    }
}
