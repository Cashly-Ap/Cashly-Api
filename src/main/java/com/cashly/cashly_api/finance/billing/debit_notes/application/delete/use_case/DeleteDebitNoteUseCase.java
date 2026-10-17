package com.cashly.cashly_api.finance.billing.debit_notes.application.delete.use_case;

import com.cashly.cashly_api.finance.billing.debit_notes.domain.entity.DebitNote;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.repository.DebitNoteRepository;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.valueobjects.DebitNoteId;
import com.cashly.cashly_api.finance.billing.debit_notes.application.ports.out.DebitNoteInvoiceGateway;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteDebitNoteUseCase {

    private final DebitNoteRepository debitNoteRepository;
    private final DebitNoteInvoiceGateway invoices;

    public DeleteDebitNoteUseCase(DebitNoteRepository debitNoteRepository,
                                  DebitNoteInvoiceGateway invoices) {
        this.debitNoteRepository = debitNoteRepository;
        this.invoices = invoices;
    }

    @Transactional
    public void execute(UUID noteId) {
        DebitNoteId id = new DebitNoteId(noteId);
        DebitNote debitNote = debitNoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Debit note not found: " + noteId));

        UUID invoiceId = debitNote.getInvoiceId();
        debitNoteRepository.delete(id);

        invoices.recalculate(invoiceId);
    }
}
