package com.cashly.cashly_api.debit_notes.application.usecases;

import com.cashly.cashly_api.debit_notes.application.dto.response.DebitNoteResponse;
import com.cashly.cashly_api.debit_notes.application.mappers.DebitNoteMapper;
import com.cashly.cashly_api.debit_notes.domain.entities.DebitNote;
import com.cashly.cashly_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.cashly.cashly_api.debit_notes.domain.valueobjects.DebitNoteId;
import com.cashly.cashly_api.invoices.domain.entities.Invoice;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetDebitNoteByIdUseCase {

    private final DebitNoteRepository debitNoteRepository;
    private final InvoiceRepository invoiceRepository;

    public GetDebitNoteByIdUseCase(DebitNoteRepository debitNoteRepository,
                                    InvoiceRepository invoiceRepository) {
        this.debitNoteRepository = debitNoteRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public DebitNoteResponse execute(UUID id) {
        DebitNote debitNote = debitNoteRepository.findById(DebitNoteId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Debit note not found: " + id));

        Invoice invoice = invoiceRepository.findById(InvoiceId.of(debitNote.getInvoiceId()))
                .orElse(null);

        String customerName = invoice != null ? invoice.getCustomerName() : null;
        String customerRif = invoice != null ? invoice.getCustomerRif() : null;
        String invoiceNumber = invoice != null ? invoice.getInvoiceNumber().value() : null;

        return DebitNoteMapper.toDebitNoteResponse(debitNote, customerName, customerRif, invoiceNumber);
    }
}
