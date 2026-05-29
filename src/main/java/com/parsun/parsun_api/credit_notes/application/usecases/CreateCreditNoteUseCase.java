package com.parsun.parsun_api.credit_notes.application.usecases;

import com.parsun.parsun_api.credit_notes.application.dto.request.CreateCreditNoteRequest;
import com.parsun.parsun_api.credit_notes.application.dto.response.CreditNoteResponse;
import com.parsun.parsun_api.credit_notes.application.mappers.CreditNoteMapper;
import com.parsun.parsun_api.credit_notes.domain.entities.CreditNote;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.credit_notes.domain.valueobjects.CreditNoteNumber;
import com.parsun.parsun_api.invoices.domain.services.InvoiceStatusService;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.UUID;

public class CreateCreditNoteUseCase {

    private final CreditNoteRepository creditNoteRepository;
    private final InvoiceStatusService invoiceStatusService;

    public CreateCreditNoteUseCase(CreditNoteRepository creditNoteRepository,
                                   InvoiceStatusService invoiceStatusService) {
        this.creditNoteRepository = creditNoteRepository;
        this.invoiceStatusService = invoiceStatusService;
    }

    @Transactional
    public CreditNoteResponse execute(CreateCreditNoteRequest request, UUID authenticatedUserId) {
        int year = LocalDate.now().getYear();
        int sequence = creditNoteRepository.getNextSequenceForYear(year);
        CreditNoteNumber noteNumber = CreditNoteNumber.of(year, sequence);

        CreditNote creditNote = CreditNote.create(
                noteNumber,
                request.customerId(),
                request.invoiceId(),
                request.amount(),
                request.reason(),
                authenticatedUserId,
                "User"
        );

        CreditNote saved = creditNoteRepository.save(creditNote);

        invoiceStatusService.recalculate(saved.getInvoiceId());

        return CreditNoteMapper.toCreditNoteResponse(saved, null, null, null);
    }
}
