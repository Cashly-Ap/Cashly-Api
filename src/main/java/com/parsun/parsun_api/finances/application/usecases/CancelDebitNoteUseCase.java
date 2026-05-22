package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.DebitNoteResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.DebitNote;
import com.parsun.parsun_api.finances.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.DebitNoteId;
import com.parsun.parsun_api.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.parsun.parsun_api.invoices.infrastructure.persistence.repositories.InvoiceJpaRepository;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class CancelDebitNoteUseCase {

    private final DebitNoteRepository debitNoteRepository;
    private final InvoiceJpaRepository invoiceJpaRepository;

    public CancelDebitNoteUseCase(DebitNoteRepository debitNoteRepository,
                                  InvoiceJpaRepository invoiceJpaRepository) {
        this.debitNoteRepository = debitNoteRepository;
        this.invoiceJpaRepository = invoiceJpaRepository;
    }

    @Transactional
    public DebitNoteResponse execute(UUID noteId) {
        DebitNoteId id = new DebitNoteId(noteId);
        DebitNote debitNote = debitNoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Debit note not found: " + noteId));

        debitNote.cancel();

        DebitNote saved = debitNoteRepository.save(debitNote);

        InvoiceEntity invoice = invoiceJpaRepository.findById(saved.getInvoiceId())
                .orElse(null);

        String customerName = invoice != null ? invoice.getCustomerName() : null;
        String customerRif = invoice != null ? invoice.getCustomerRif() : null;
        String invoiceNumber = invoice != null ? invoice.getInvoiceNumber() : null;

        return FinanceMapper.toDebitNoteResponse(saved, customerName, customerRif, invoiceNumber);
    }
}
