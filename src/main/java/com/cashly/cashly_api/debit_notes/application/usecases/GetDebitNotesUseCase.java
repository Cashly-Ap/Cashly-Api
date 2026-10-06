package com.cashly.cashly_api.debit_notes.application.usecases;

import com.cashly.cashly_api.debit_notes.application.dto.response.DebitNoteResponse;
import com.cashly.cashly_api.debit_notes.application.mappers.DebitNoteMapper;
import com.cashly.cashly_api.debit_notes.domain.entities.DebitNote;
import com.cashly.cashly_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.cashly.cashly_api.invoices.domain.entities.Invoice;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class GetDebitNotesUseCase {

    private final DebitNoteRepository debitNoteRepository;
    private final InvoiceRepository invoiceRepository;

    public GetDebitNotesUseCase(DebitNoteRepository debitNoteRepository,
                                  InvoiceRepository invoiceRepository) {
        this.debitNoteRepository = debitNoteRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public List<DebitNoteResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                              UUID customerId) {
        List<DebitNote> debitNotes = debitNoteRepository.findAllWithFilters(dateFrom, dateTo, customerId);

        List<InvoiceId> invoiceIds = debitNotes.stream()
                .map(DebitNote::getInvoiceId)
                .distinct()
                .map(InvoiceId::of)
                .toList();

        Map<UUID, Invoice> invoices = invoiceRepository.findAllById(invoiceIds).stream()
                .collect(Collectors.toMap(inv -> inv.getId().value(), inv -> inv));

        return debitNotes.stream()
                .map(dn -> {
                    Invoice invoice = invoices.get(dn.getInvoiceId());
                    String customerName = invoice != null ? invoice.getCustomerName() : null;
                    String customerRif = invoice != null ? invoice.getCustomerRif() : null;
                    String invoiceNumber = invoice != null ? invoice.getInvoiceNumber().value() : null;
                    return DebitNoteMapper.toDebitNoteResponse(dn, customerName, customerRif, invoiceNumber);
                })
                .toList();
    }
}
