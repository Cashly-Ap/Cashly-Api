package com.cashly.cashly_api.debit_notes.application.usecases;

import com.cashly.cashly_api.debit_notes.application.dto.response.DebitNoteResponse;
import com.cashly.cashly_api.debit_notes.application.contracts.DebitNoteInvoiceGateway;
import com.cashly.cashly_api.debit_notes.application.mappers.DebitNoteMapper;
import com.cashly.cashly_api.debit_notes.domain.entities.DebitNote;
import com.cashly.cashly_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.cashly.cashly_api.debit_notes.domain.valueobjects.DebitNoteId;

import java.util.UUID;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class GetDebitNoteByIdUseCase {

    private final DebitNoteRepository debitNoteRepository;
    private final DebitNoteInvoiceGateway invoices;

    public GetDebitNoteByIdUseCase(DebitNoteRepository debitNoteRepository,
                                    DebitNoteInvoiceGateway invoices) {
        this.debitNoteRepository = debitNoteRepository;
        this.invoices = invoices;
    }

    public DebitNoteResponse execute(UUID id) {
        DebitNote debitNote = debitNoteRepository.findById(DebitNoteId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Debit note not found: " + id));

        var invoice = invoices.findAll(Set.of(debitNote.getInvoiceId())).get(debitNote.getInvoiceId());

        String customerName = invoice != null ? invoice.customerName() : null;
        String customerRif = invoice != null ? invoice.customerRif() : null;
        String invoiceNumber = invoice != null ? invoice.number() : null;

        return DebitNoteMapper.toDebitNoteResponse(debitNote, customerName, customerRif, invoiceNumber);
    }
}
