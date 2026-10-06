package com.cashly.cashly_api.credit_notes.application.usecases;

import com.cashly.cashly_api.credit_notes.domain.entities.CreditNote;
import com.cashly.cashly_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.cashly.cashly_api.credit_notes.domain.valueobjects.CreditNoteId;
import com.cashly.cashly_api.invoices.domain.services.InvoiceStatusService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteCreditNoteUseCase {

    private final CreditNoteRepository creditNoteRepository;
    private final InvoiceStatusService invoiceStatusService;

    public DeleteCreditNoteUseCase(CreditNoteRepository creditNoteRepository,
                                   InvoiceStatusService invoiceStatusService) {
        this.creditNoteRepository = creditNoteRepository;
        this.invoiceStatusService = invoiceStatusService;
    }

    @Transactional
    public void execute(UUID noteId) {
        CreditNoteId id = new CreditNoteId(noteId);
        CreditNote creditNote = creditNoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Credit note not found: " + noteId));

        UUID invoiceId = creditNote.getInvoiceId();
        creditNoteRepository.delete(id);

        invoiceStatusService.recalculate(invoiceId);
    }
}
