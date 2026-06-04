package com.parsun.parsun_api.credit_notes.application.usecases;

import com.parsun.parsun_api.credit_notes.application.dto.request.CreateCreditNoteRequest;
import com.parsun.parsun_api.credit_notes.application.dto.response.CreditNoteResponse;
import com.parsun.parsun_api.credit_notes.application.mappers.CreditNoteMapper;
import com.parsun.parsun_api.credit_notes.domain.entities.CreditNote;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.credit_notes.domain.valueobjects.CreditNoteNumber;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.services.InvoiceStatusService;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.UUID;

public class CreateCreditNoteUseCase {

    private final CreditNoteRepository creditNoteRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceStatusService invoiceStatusService;

    public CreateCreditNoteUseCase(CreditNoteRepository creditNoteRepository,
                                   InvoiceRepository invoiceRepository,
                                   InvoiceStatusService invoiceStatusService) {
        this.creditNoteRepository = creditNoteRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceStatusService = invoiceStatusService;
    }

    @Transactional
    public CreditNoteResponse execute(CreateCreditNoteRequest request, UUID authenticatedUserId) {
        Invoice invoice = invoiceRepository.findById(new InvoiceId(request.invoiceId()))
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + request.invoiceId()));

        if (invoice.getStatus() == InvoiceStatus.CANCELLED || invoice.getStatus() == InvoiceStatus.FULLY_RETURNED) {
            throw new IllegalStateException(
                    "Cannot create credit note for invoice " + invoice.getInvoiceNumber().value()
                    + ". Current status: " + invoice.getStatus().label()
            );
        }

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
