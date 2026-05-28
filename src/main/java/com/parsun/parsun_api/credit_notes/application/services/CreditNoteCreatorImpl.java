package com.parsun.parsun_api.credit_notes.application.services;

import com.parsun.parsun_api.credit_notes.domain.entities.CreditNote;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.credit_notes.domain.valueobjects.CreditNoteNumber;
import com.parsun.parsun_api.returns.domain.services.CreditNoteCreator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CreditNoteCreatorImpl implements CreditNoteCreator {

    private final CreditNoteRepository creditNoteRepository;

    public CreditNoteCreatorImpl(CreditNoteRepository creditNoteRepository) {
        this.creditNoteRepository = creditNoteRepository;
    }

    @Override
    public void createCreditNote(UUID customerId, UUID invoiceId, BigDecimal amount,
                                  String reason, UUID createdBy, String createdByName) {
        int year = LocalDate.now().getYear();
        int sequence = creditNoteRepository.getNextSequenceForYear(year);
        CreditNoteNumber noteNumber = CreditNoteNumber.of(year, sequence);

        CreditNote creditNote = CreditNote.create(
                noteNumber,
                customerId,
                invoiceId,
                amount,
                reason,
                createdBy,
                createdByName
        );

        creditNoteRepository.save(creditNote);
    }
}
