package com.cashly.cashly_api.finance.billing.credit_notes.application.list.use_case;

import com.cashly.cashly_api.finance.billing.credit_notes.application.dto.response.CreditNoteResponse;
import com.cashly.cashly_api.finance.billing.credit_notes.application.ports.out.CreditNoteInvoiceGateway;
import com.cashly.cashly_api.finance.billing.credit_notes.application.mappers.CreditNoteMapper;
import com.cashly.cashly_api.finance.billing.credit_notes.domain.entity.CreditNote;
import com.cashly.cashly_api.finance.billing.credit_notes.domain.repository.CreditNoteRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class GetCreditNotesUseCase {

    private final CreditNoteRepository creditNoteRepository;
    private final CreditNoteInvoiceGateway invoices;

    public GetCreditNotesUseCase(CreditNoteRepository creditNoteRepository,
                                  CreditNoteInvoiceGateway invoices) {
        this.creditNoteRepository = creditNoteRepository;
        this.invoices = invoices;
    }

    public List<CreditNoteResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                              UUID customerId) {
        List<CreditNote> creditNotes = creditNoteRepository.findAllWithFilters(dateFrom, dateTo, customerId);

        Set<UUID> invoiceIds = creditNotes.stream()
                .map(CreditNote::getInvoiceId)
                .distinct()
                .collect(Collectors.toSet());

        var invoiceContexts = invoices.findAll(invoiceIds);

        return creditNotes.stream()
                .map(cn -> {
                    var invoice = invoiceContexts.get(cn.getInvoiceId());
                    String customerName = invoice != null ? invoice.customerName() : null;
                    String customerRif = invoice != null ? invoice.customerRif() : null;
                    String invoiceNumber = invoice != null ? invoice.number() : null;
                    return CreditNoteMapper.toCreditNoteResponse(cn, customerName, customerRif, invoiceNumber);
                })
                .toList();
    }
}
