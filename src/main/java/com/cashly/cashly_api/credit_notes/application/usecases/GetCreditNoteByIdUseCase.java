package com.cashly.cashly_api.credit_notes.application.usecases;

import com.cashly.cashly_api.credit_notes.application.dto.response.CreditNoteResponse;
import com.cashly.cashly_api.credit_notes.application.contracts.CreditNoteInvoiceGateway;
import com.cashly.cashly_api.credit_notes.application.mappers.CreditNoteMapper;
import com.cashly.cashly_api.credit_notes.domain.entities.CreditNote;
import com.cashly.cashly_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.cashly.cashly_api.credit_notes.domain.valueobjects.CreditNoteId;

import java.util.UUID;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class GetCreditNoteByIdUseCase {

    private final CreditNoteRepository creditNoteRepository;
    private final CreditNoteInvoiceGateway invoices;

    public GetCreditNoteByIdUseCase(CreditNoteRepository creditNoteRepository,
                                    CreditNoteInvoiceGateway invoices) {
        this.creditNoteRepository = creditNoteRepository;
        this.invoices = invoices;
    }

    public CreditNoteResponse execute(UUID id) {
        CreditNote creditNote = creditNoteRepository.findById(CreditNoteId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Credit note not found: " + id));

        var invoice = invoices.findAll(Set.of(creditNote.getInvoiceId())).get(creditNote.getInvoiceId());

        String customerName = invoice != null ? invoice.customerName() : null;
        String customerRif = invoice != null ? invoice.customerRif() : null;
        String invoiceNumber = invoice != null ? invoice.number() : null;

        return CreditNoteMapper.toCreditNoteResponse(creditNote, customerName, customerRif, invoiceNumber);
    }
}
