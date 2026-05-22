package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.DebitNoteResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.DebitNote;
import com.parsun.parsun_api.finances.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.DebitNoteId;
import com.parsun.parsun_api.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.parsun.parsun_api.invoices.infrastructure.persistence.repositories.InvoiceJpaRepository;

import java.util.UUID;

public class GetDebitNoteByIdUseCase {

    private final DebitNoteRepository debitNoteRepository;
    private final InvoiceJpaRepository invoiceJpaRepository;

    public GetDebitNoteByIdUseCase(DebitNoteRepository debitNoteRepository,
                                   InvoiceJpaRepository invoiceJpaRepository) {
        this.debitNoteRepository = debitNoteRepository;
        this.invoiceJpaRepository = invoiceJpaRepository;
    }

    public DebitNoteResponse execute(UUID id) {
        DebitNote debitNote = debitNoteRepository.findById(DebitNoteId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Debit note not found: " + id));

        InvoiceEntity invoice = invoiceJpaRepository.findById(debitNote.getInvoiceId())
                .orElse(null);

        String customerName = invoice != null ? invoice.getCustomerName() : null;
        String customerRif = invoice != null ? invoice.getCustomerRif() : null;
        String invoiceNumber = invoice != null ? invoice.getInvoiceNumber() : null;

        return FinanceMapper.toDebitNoteResponse(debitNote, customerName, customerRif, invoiceNumber);
    }
}
