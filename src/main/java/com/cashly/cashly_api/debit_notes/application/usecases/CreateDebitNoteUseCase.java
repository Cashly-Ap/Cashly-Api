package com.cashly.cashly_api.debit_notes.application.usecases;

import com.cashly.cashly_api.debit_notes.application.contracts.DebitNoteCashGateway;
import com.cashly.cashly_api.debit_notes.application.contracts.DebitNoteInvoiceGateway;
import com.cashly.cashly_api.debit_notes.application.dto.request.CreateDebitNoteRequest;
import com.cashly.cashly_api.debit_notes.application.dto.response.DebitNoteResponse;
import com.cashly.cashly_api.debit_notes.application.mappers.DebitNoteMapper;
import com.cashly.cashly_api.debit_notes.domain.entities.DebitNote;
import com.cashly.cashly_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.cashly.cashly_api.debit_notes.domain.valueobjects.DebitNoteNumber;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class CreateDebitNoteUseCase {
    private final DebitNoteRepository debitNoteRepository;
    private final DebitNoteInvoiceGateway invoices;
    private final DebitNoteCashGateway cash;

    public CreateDebitNoteUseCase(DebitNoteRepository debitNoteRepository,
                                  DebitNoteInvoiceGateway invoices,
                                  DebitNoteCashGateway cash) {
        this.debitNoteRepository = debitNoteRepository;
        this.invoices = invoices;
        this.cash = cash;
    }

    @Transactional
    public DebitNoteResponse execute(CreateDebitNoteRequest request, UUID authenticatedUserId) {
        DebitNoteInvoiceGateway.InvoiceContext invoice = invoices.find(request.invoiceId());
        if ("CANCELLED".equals(invoice.status()) || "FULLY_RETURNED".equals(invoice.status())) {
            throw new IllegalStateException("Cannot create debit note for invoice " + invoice.number()
                    + ". Current status: " + invoice.statusLabel());
        }

        int year = LocalDate.now().getYear();
        DebitNoteNumber noteNumber = DebitNoteNumber.of(year,
                debitNoteRepository.getNextSequenceForYear(year));
        DebitNote note = DebitNote.create(noteNumber, request.customerId(), request.invoiceId(),
                request.amount(), request.reason(), authenticatedUserId, "User");
        DebitNote saved = debitNoteRepository.save(note);
        cash.recordCashIn(saved.getAmount(), saved.getNoteNumber().value(), saved.getId().value(),
                authenticatedUserId, saved.getCreatedByName());
        invoices.recalculate(saved.getInvoiceId());
        return DebitNoteMapper.toDebitNoteResponse(saved, null, null, null);
    }
}
