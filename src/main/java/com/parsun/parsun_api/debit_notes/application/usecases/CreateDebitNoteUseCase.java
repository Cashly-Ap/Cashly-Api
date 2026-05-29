package com.parsun.parsun_api.debit_notes.application.usecases;

import com.parsun.parsun_api.debit_notes.application.dto.request.CreateDebitNoteRequest;
import com.parsun.parsun_api.debit_notes.application.dto.response.DebitNoteResponse;
import com.parsun.parsun_api.debit_notes.application.mappers.DebitNoteMapper;
import com.parsun.parsun_api.debit_notes.domain.entities.DebitNote;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.debit_notes.domain.valueobjects.DebitNoteNumber;
import com.parsun.parsun_api.invoices.domain.services.InvoiceStatusService;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.UUID;

public class CreateDebitNoteUseCase {

    private final DebitNoteRepository debitNoteRepository;
    private final InvoiceStatusService invoiceStatusService;

    public CreateDebitNoteUseCase(DebitNoteRepository debitNoteRepository,
                                  InvoiceStatusService invoiceStatusService) {
        this.debitNoteRepository = debitNoteRepository;
        this.invoiceStatusService = invoiceStatusService;
    }

    @Transactional
    public DebitNoteResponse execute(CreateDebitNoteRequest request, UUID authenticatedUserId) {
        int year = LocalDate.now().getYear();
        int sequence = debitNoteRepository.getNextSequenceForYear(year);
        DebitNoteNumber noteNumber = DebitNoteNumber.of(year, sequence);

        DebitNote debitNote = DebitNote.create(
                noteNumber,
                request.customerId(),
                request.invoiceId(),
                request.amount(),
                request.reason(),
                authenticatedUserId,
                "User"
        );

        DebitNote saved = debitNoteRepository.save(debitNote);

        invoiceStatusService.recalculate(saved.getInvoiceId());

        return DebitNoteMapper.toDebitNoteResponse(saved, null, null, null);
    }
}
