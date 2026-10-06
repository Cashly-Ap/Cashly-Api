package com.cashly.cashly_api.debit_notes.application.mappers;

import com.cashly.cashly_api.debit_notes.application.dto.response.DebitNoteResponse;
import com.cashly.cashly_api.debit_notes.domain.entities.DebitNote;

public class DebitNoteMapper {

    private DebitNoteMapper() {}

    public static DebitNoteResponse toDebitNoteResponse(DebitNote debitNote,
                                                         String customerName,
                                                         String customerRif,
                                                         String invoiceNumber) {
        return new DebitNoteResponse(
                debitNote.getId().value(),
                debitNote.getNoteNumber().value(),
                debitNote.getCustomerId(),
                customerName,
                customerRif,
                debitNote.getInvoiceId(),
                invoiceNumber,
                debitNote.getAmount(),
                debitNote.getReason(),
                debitNote.getCreatedBy(),
                debitNote.getCreatedByName(),
                debitNote.getCreatedAt(),
                debitNote.getUpdatedAt()
        );
    }
}
