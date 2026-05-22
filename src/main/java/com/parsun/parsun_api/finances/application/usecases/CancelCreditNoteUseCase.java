package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.CreditNoteResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.CreditNote;
import com.parsun.parsun_api.finances.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.CreditNoteId;
import com.parsun.parsun_api.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.parsun.parsun_api.invoices.infrastructure.persistence.repositories.InvoiceJpaRepository;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class CancelCreditNoteUseCase {

    private final CreditNoteRepository creditNoteRepository;
    private final InvoiceJpaRepository invoiceJpaRepository;

    public CancelCreditNoteUseCase(CreditNoteRepository creditNoteRepository,
                                   InvoiceJpaRepository invoiceJpaRepository) {
        this.creditNoteRepository = creditNoteRepository;
        this.invoiceJpaRepository = invoiceJpaRepository;
    }

    @Transactional
    public CreditNoteResponse execute(UUID noteId) {
        CreditNoteId id = new CreditNoteId(noteId);
        CreditNote creditNote = creditNoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Credit note not found: " + noteId));

        creditNote.cancel();

        CreditNote saved = creditNoteRepository.save(creditNote);

        InvoiceEntity invoice = invoiceJpaRepository.findById(saved.getInvoiceId())
                .orElse(null);

        String customerName = invoice != null ? invoice.getCustomerName() : null;
        String customerRif = invoice != null ? invoice.getCustomerRif() : null;
        String invoiceNumber = invoice != null ? invoice.getInvoiceNumber() : null;

        return FinanceMapper.toCreditNoteResponse(saved, customerName, customerRif, invoiceNumber);
    }
}
