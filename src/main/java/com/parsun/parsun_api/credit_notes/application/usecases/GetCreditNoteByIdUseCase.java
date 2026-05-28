package com.parsun.parsun_api.credit_notes.application.usecases;

import com.parsun.parsun_api.credit_notes.application.dto.response.CreditNoteResponse;
import com.parsun.parsun_api.credit_notes.application.mappers.CreditNoteMapper;
import com.parsun.parsun_api.credit_notes.domain.entities.CreditNote;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.credit_notes.domain.valueobjects.CreditNoteId;
import com.parsun.parsun_api.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.parsun.parsun_api.invoices.infrastructure.persistence.repositories.InvoiceJpaRepository;

import java.util.UUID;

public class GetCreditNoteByIdUseCase {

    private final CreditNoteRepository creditNoteRepository;
    private final InvoiceJpaRepository invoiceJpaRepository;

    public GetCreditNoteByIdUseCase(CreditNoteRepository creditNoteRepository,
                                    InvoiceJpaRepository invoiceJpaRepository) {
        this.creditNoteRepository = creditNoteRepository;
        this.invoiceJpaRepository = invoiceJpaRepository;
    }

    public CreditNoteResponse execute(UUID id) {
        CreditNote creditNote = creditNoteRepository.findById(CreditNoteId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Credit note not found: " + id));

        InvoiceEntity invoice = invoiceJpaRepository.findById(creditNote.getInvoiceId())
                .orElse(null);

        String customerName = invoice != null ? invoice.getCustomerName() : null;
        String customerRif = invoice != null ? invoice.getCustomerRif() : null;
        String invoiceNumber = invoice != null ? invoice.getInvoiceNumber() : null;

        return CreditNoteMapper.toCreditNoteResponse(creditNote, customerName, customerRif, invoiceNumber);
    }
}
