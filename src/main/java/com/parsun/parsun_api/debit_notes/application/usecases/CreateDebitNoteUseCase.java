package com.parsun.parsun_api.debit_notes.application.usecases;

import com.parsun.parsun_api.debit_notes.application.dto.request.CreateDebitNoteRequest;
import com.parsun.parsun_api.debit_notes.application.dto.response.DebitNoteResponse;
import com.parsun.parsun_api.debit_notes.application.mappers.DebitNoteMapper;
import com.parsun.parsun_api.debit_notes.domain.entities.DebitNote;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.debit_notes.domain.valueobjects.DebitNoteNumber;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.services.InvoiceStatusService;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class CreateDebitNoteUseCase {

    private final DebitNoteRepository debitNoteRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceStatusService invoiceStatusService;

    public CreateDebitNoteUseCase(DebitNoteRepository debitNoteRepository,
                                  InvoiceRepository invoiceRepository,
                                  InvoiceStatusService invoiceStatusService) {
        this.debitNoteRepository = debitNoteRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceStatusService = invoiceStatusService;
    }

    @Transactional
    public DebitNoteResponse execute(CreateDebitNoteRequest request, UUID authenticatedUserId) {
        Invoice invoice = invoiceRepository.findById(new InvoiceId(request.invoiceId()))
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + request.invoiceId()));

        if (invoice.getStatus() == InvoiceStatus.CANCELLED || invoice.getStatus() == InvoiceStatus.FULLY_RETURNED) {
            throw new IllegalStateException(
                    "Cannot create debit note for invoice " + invoice.getInvoiceNumber().value()
                    + ". Current status: " + invoice.getStatus().label()
            );
        }

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
