package com.cashly.cashly_api.debit_notes.application.usecases;

import com.cashly.cashly_api.cashregister.application.services.CashMovementService;
import com.cashly.cashly_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cashregister.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.debit_notes.application.dto.request.CreateDebitNoteRequest;
import com.cashly.cashly_api.debit_notes.application.dto.response.DebitNoteResponse;
import com.cashly.cashly_api.debit_notes.application.mappers.DebitNoteMapper;
import com.cashly.cashly_api.debit_notes.domain.entities.DebitNote;
import com.cashly.cashly_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.cashly.cashly_api.debit_notes.domain.valueobjects.DebitNoteNumber;
import com.cashly.cashly_api.invoices.domain.entities.Invoice;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.services.InvoiceStatusService;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceStatus;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class CreateDebitNoteUseCase {

    private final DebitNoteRepository debitNoteRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceStatusService invoiceStatusService;
    private final CashMovementService cashMovementService;

    public CreateDebitNoteUseCase(DebitNoteRepository debitNoteRepository,
                                  InvoiceRepository invoiceRepository,
                                  InvoiceStatusService invoiceStatusService,
                                  CashMovementService cashMovementService) {
        this.debitNoteRepository = debitNoteRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceStatusService = invoiceStatusService;
        this.cashMovementService = cashMovementService;
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
        cashMovementService.record(
                CashMovementDirection.IN,
                CashMovementType.DEBIT_NOTE,
                saved.getAmount(),
                null,
                "Nota de débito " + saved.getNoteNumber().value(),
                "DEBIT_NOTE",
                saved.getId().value(),
                authenticatedUserId,
                saved.getCreatedByName()
        );

        invoiceStatusService.recalculate(saved.getInvoiceId());

        return DebitNoteMapper.toDebitNoteResponse(saved, null, null, null);
    }
}
