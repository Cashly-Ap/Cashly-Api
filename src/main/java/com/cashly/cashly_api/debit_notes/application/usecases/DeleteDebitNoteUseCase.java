package com.cashly.cashly_api.debit_notes.application.usecases;

import com.cashly.cashly_api.debit_notes.domain.entities.DebitNote;
import com.cashly.cashly_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.cashly.cashly_api.debit_notes.domain.valueobjects.DebitNoteId;
import com.cashly.cashly_api.invoices.domain.services.InvoiceStatusService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteDebitNoteUseCase {

    private final DebitNoteRepository debitNoteRepository;
    private final InvoiceStatusService invoiceStatusService;

    public DeleteDebitNoteUseCase(DebitNoteRepository debitNoteRepository,
                                  InvoiceStatusService invoiceStatusService) {
        this.debitNoteRepository = debitNoteRepository;
        this.invoiceStatusService = invoiceStatusService;
    }

    @Transactional
    public void execute(UUID noteId) {
        DebitNoteId id = new DebitNoteId(noteId);
        DebitNote debitNote = debitNoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Debit note not found: " + noteId));

        UUID invoiceId = debitNote.getInvoiceId();
        debitNoteRepository.delete(id);

        invoiceStatusService.recalculate(invoiceId);
    }
}
