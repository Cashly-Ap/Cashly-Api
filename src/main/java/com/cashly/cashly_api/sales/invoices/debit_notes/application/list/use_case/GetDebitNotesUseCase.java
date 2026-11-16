package com.cashly.cashly_api.sales.invoices.debit_notes.application.list.use_case;

import com.cashly.cashly_api.sales.invoices.debit_notes.application.dto.response.DebitNoteResponse;
import com.cashly.cashly_api.sales.invoices.debit_notes.application.ports.out.DebitNoteInvoiceGateway;
import com.cashly.cashly_api.sales.invoices.debit_notes.application.mappers.DebitNoteMapper;
import com.cashly.cashly_api.sales.invoices.debit_notes.domain.entity.DebitNote;
import com.cashly.cashly_api.sales.invoices.debit_notes.domain.repository.DebitNoteRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class GetDebitNotesUseCase {

    private final DebitNoteRepository debitNoteRepository;
    private final DebitNoteInvoiceGateway invoices;

    public GetDebitNotesUseCase(DebitNoteRepository debitNoteRepository,
            DebitNoteInvoiceGateway invoices) {
        this.debitNoteRepository = debitNoteRepository;
        this.invoices = invoices;
    }

    public List<DebitNoteResponse> execute(LocalDate dateFrom, LocalDate dateTo,
            UUID customerId) {
        List<DebitNote> debitNotes = debitNoteRepository
                .findAllWithFilters(dateFrom, dateTo, customerId);

        Set<UUID> invoiceIds = debitNotes.stream().map(DebitNote::getInvoiceId)
                .distinct().collect(Collectors.toSet());

        var invoiceContexts = invoices.findAll(invoiceIds);

        return debitNotes.stream().map(dn -> {
            var invoice = invoiceContexts.get(dn.getInvoiceId());
            String customerName = invoice != null
                    ? invoice.customerName()
                    : null;
            String customerRif = invoice != null ? invoice.customerRif() : null;
            String invoiceNumber = invoice != null ? invoice.number() : null;
            return DebitNoteMapper.toDebitNoteResponse(dn, customerName,
                    customerRif, invoiceNumber);
        }).toList();
    }

    public PageResponse<DebitNoteResponse> executePage(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, String search, int page,
            int size, String orderBy, String orderDir) {
        PageResponse<DebitNote> debitNotes = debitNoteRepository
                .findPageWithFilters(dateFrom, dateTo, customerId, search, page,
                        size, orderBy, orderDir);
        Set<UUID> invoiceIds = debitNotes.content().stream()
                .map(DebitNote::getInvoiceId).distinct()
                .collect(Collectors.toSet());
        var invoiceContexts = invoices.findAll(invoiceIds);
        List<DebitNoteResponse> content = debitNotes.content().stream()
                .map(dn -> toResponse(dn, invoiceContexts))
                .toList();
        return new PageResponse<>(content, debitNotes.totalElements(),
                debitNotes.totalPages(), debitNotes.size(),
                debitNotes.number(), debitNotes.first(), debitNotes.last(),
                content.isEmpty());
    }

    private static DebitNoteResponse toResponse(DebitNote debitNote,
            java.util.Map<UUID, com.cashly.cashly_api.sales.invoices.debit_notes.application.ports.out.DebitNoteInvoiceGateway.InvoiceContext> invoiceContexts) {
        var invoice = invoiceContexts.get(debitNote.getInvoiceId());
        return DebitNoteMapper.toDebitNoteResponse(debitNote,
                invoice != null ? invoice.customerName() : null,
                invoice != null ? invoice.customerRif() : null,
                invoice != null ? invoice.number() : null);
    }
}
