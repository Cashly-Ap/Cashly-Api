package com.parsun.parsun_api.credit_notes.application.usecases;

import com.parsun.parsun_api.credit_notes.application.dto.response.CreditNoteResponse;
import com.parsun.parsun_api.credit_notes.application.mappers.CreditNoteMapper;
import com.parsun.parsun_api.credit_notes.domain.entities.CreditNote;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.credit_notes.domain.valueobjects.CreditNoteId;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetCreditNoteByIdUseCase {

    private final CreditNoteRepository creditNoteRepository;
    private final InvoiceRepository invoiceRepository;

    public GetCreditNoteByIdUseCase(CreditNoteRepository creditNoteRepository,
                                    InvoiceRepository invoiceRepository) {
        this.creditNoteRepository = creditNoteRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public CreditNoteResponse execute(UUID id) {
        CreditNote creditNote = creditNoteRepository.findById(CreditNoteId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Credit note not found: " + id));

        Invoice invoice = invoiceRepository.findById(InvoiceId.of(creditNote.getInvoiceId()))
                .orElse(null);

        String customerName = invoice != null ? invoice.getCustomerName() : null;
        String customerRif = invoice != null ? invoice.getCustomerRif() : null;
        String invoiceNumber = invoice != null ? invoice.getInvoiceNumber().value() : null;

        return CreditNoteMapper.toCreditNoteResponse(creditNote, customerName, customerRif, invoiceNumber);
    }
}
