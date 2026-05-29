package com.parsun.parsun_api.debit_notes.application.usecases;

import com.parsun.parsun_api.debit_notes.domain.entities.DebitNote;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.debit_notes.domain.valueobjects.DebitNoteId;
import com.parsun.parsun_api.invoices.domain.services.InvoiceStatusService;
import jakarta.transaction.Transactional;

import java.util.UUID;

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
