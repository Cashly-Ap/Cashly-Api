package com.parsun.parsun_api.credit_notes.application.usecases;

import com.parsun.parsun_api.credit_notes.application.dto.response.CreditNoteResponse;
import com.parsun.parsun_api.credit_notes.application.mappers.CreditNoteMapper;
import com.parsun.parsun_api.credit_notes.domain.entities.CreditNote;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class GetCreditNotesUseCase {

    private final CreditNoteRepository creditNoteRepository;
    private final InvoiceRepository invoiceRepository;

    public GetCreditNotesUseCase(CreditNoteRepository creditNoteRepository,
                                  InvoiceRepository invoiceRepository) {
        this.creditNoteRepository = creditNoteRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public List<CreditNoteResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                              UUID customerId) {
        List<CreditNote> creditNotes = creditNoteRepository.findAllWithFilters(dateFrom, dateTo, customerId);

        List<InvoiceId> invoiceIds = creditNotes.stream()
                .map(CreditNote::getInvoiceId)
                .distinct()
                .map(InvoiceId::of)
                .toList();

        Map<UUID, Invoice> invoices = invoiceRepository.findAllById(invoiceIds).stream()
                .collect(Collectors.toMap(inv -> inv.getId().value(), inv -> inv));

        return creditNotes.stream()
                .map(cn -> {
                    Invoice invoice = invoices.get(cn.getInvoiceId());
                    String customerName = invoice != null ? invoice.getCustomerName() : null;
                    String customerRif = invoice != null ? invoice.getCustomerRif() : null;
                    String invoiceNumber = invoice != null ? invoice.getInvoiceNumber().value() : null;
                    return CreditNoteMapper.toCreditNoteResponse(cn, customerName, customerRif, invoiceNumber);
                })
                .toList();
    }
}
