package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.CreditNoteResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.CreditNote;
import com.parsun.parsun_api.finances.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.parsun.parsun_api.invoices.infrastructure.persistence.repositories.InvoiceJpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetCreditNotesUseCase {

    private final CreditNoteRepository creditNoteRepository;
    private final InvoiceJpaRepository invoiceJpaRepository;

    public GetCreditNotesUseCase(CreditNoteRepository creditNoteRepository,
                                 InvoiceJpaRepository invoiceJpaRepository) {
        this.creditNoteRepository = creditNoteRepository;
        this.invoiceJpaRepository = invoiceJpaRepository;
    }

    public List<CreditNoteResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                              UUID customerId) {
        List<CreditNote> creditNotes = creditNoteRepository.findAllWithFilters(dateFrom, dateTo, customerId);

        List<UUID> invoiceIds = creditNotes.stream()
                .map(CreditNote::getInvoiceId)
                .distinct()
                .toList();

        Map<UUID, InvoiceEntity> invoices = invoiceJpaRepository.findAllById(invoiceIds).stream()
                .collect(Collectors.toMap(InvoiceEntity::getId, inv -> inv));

        return creditNotes.stream()
                .map(cn -> {
                    InvoiceEntity invoice = invoices.get(cn.getInvoiceId());
                    String customerName = invoice != null ? invoice.getCustomerName() : null;
                    String customerRif = invoice != null ? invoice.getCustomerRif() : null;
                    String invoiceNumber = invoice != null ? invoice.getInvoiceNumber() : null;
                    return FinanceMapper.toCreditNoteResponse(cn, customerName, customerRif, invoiceNumber);
                })
                .toList();
    }
}
