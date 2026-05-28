package com.parsun.parsun_api.debit_notes.application.usecases;

import com.parsun.parsun_api.debit_notes.application.dto.response.DebitNoteResponse;
import com.parsun.parsun_api.debit_notes.application.mappers.DebitNoteMapper;
import com.parsun.parsun_api.debit_notes.domain.entities.DebitNote;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.parsun.parsun_api.invoices.infrastructure.persistence.repositories.InvoiceJpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetDebitNotesUseCase {

    private final DebitNoteRepository debitNoteRepository;
    private final InvoiceJpaRepository invoiceJpaRepository;

    public GetDebitNotesUseCase(DebitNoteRepository debitNoteRepository,
                                 InvoiceJpaRepository invoiceJpaRepository) {
        this.debitNoteRepository = debitNoteRepository;
        this.invoiceJpaRepository = invoiceJpaRepository;
    }

    public List<DebitNoteResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                              UUID customerId) {
        List<DebitNote> debitNotes = debitNoteRepository.findAllWithFilters(dateFrom, dateTo, customerId);

        List<UUID> invoiceIds = debitNotes.stream()
                .map(DebitNote::getInvoiceId)
                .distinct()
                .toList();

        Map<UUID, InvoiceEntity> invoices = invoiceJpaRepository.findAllById(invoiceIds).stream()
                .collect(Collectors.toMap(InvoiceEntity::getId, inv -> inv));

        return debitNotes.stream()
                .map(dn -> {
                    InvoiceEntity invoice = invoices.get(dn.getInvoiceId());
                    String customerName = invoice != null ? invoice.getCustomerName() : null;
                    String customerRif = invoice != null ? invoice.getCustomerRif() : null;
                    String invoiceNumber = invoice != null ? invoice.getInvoiceNumber() : null;
                    return DebitNoteMapper.toDebitNoteResponse(dn, customerName, customerRif, invoiceNumber);
                })
                .toList();
    }
}
