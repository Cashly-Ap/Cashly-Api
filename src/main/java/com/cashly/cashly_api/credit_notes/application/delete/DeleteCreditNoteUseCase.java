package com.cashly.cashly_api.credit_notes.application.delete;

import com.cashly.cashly_api.credit_notes.domain.CreditNote;
import com.cashly.cashly_api.credit_notes.domain.CreditNoteRepository;
import com.cashly.cashly_api.credit_notes.domain.valueobjects.CreditNoteId;
import com.cashly.cashly_api.credit_notes.application.ports.out.CreditNoteInvoiceGateway;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteCreditNoteUseCase {

    private final CreditNoteRepository creditNoteRepository;
    private final CreditNoteInvoiceGateway invoices;

    public DeleteCreditNoteUseCase(CreditNoteRepository creditNoteRepository,
                                   CreditNoteInvoiceGateway invoices) {
        this.creditNoteRepository = creditNoteRepository;
        this.invoices = invoices;
    }

    @Transactional
    public void execute(UUID noteId) {
        CreditNoteId id = new CreditNoteId(noteId);
        CreditNote creditNote = creditNoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Credit note not found: " + noteId));

        UUID invoiceId = creditNote.getInvoiceId();
        creditNoteRepository.delete(id);

        invoices.recalculate(invoiceId);
    }
}
