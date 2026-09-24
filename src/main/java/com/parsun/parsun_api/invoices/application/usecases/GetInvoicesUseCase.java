package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.invoices.application.dto.response.InvoiceListResponse;
import com.parsun.parsun_api.invoices.application.mappers.InvoiceMapper;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetInvoicesUseCase {

    private static final List<String> DEFAULT_STATUSES = List.of("DRAFT", "PENDING_PAYMENT", "PARTIALLY_PAID");

    private final InvoiceRepository invoiceRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final DebitNoteRepository debitNoteRepository;
    private final CreditNoteRepository creditNoteRepository;

    public GetInvoicesUseCase(InvoiceRepository invoiceRepository,
                               PaymentAllocationRepository paymentAllocationRepository,
                               DebitNoteRepository debitNoteRepository,
                               CreditNoteRepository creditNoteRepository) {
        this.invoiceRepository = invoiceRepository;
        this.paymentAllocationRepository = paymentAllocationRepository;
        this.debitNoteRepository = debitNoteRepository;
        this.creditNoteRepository = creditNoteRepository;
    }

    public List<InvoiceListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                              UUID customerId, List<String> statuses, String search,
                                              List<String> returnStatus,
                                              String dueStatus,
                                              UUID productId, UUID categoryId,
                                              String orderBy, String orderDir) {
        List<String> effectiveStatuses = (statuses == null || statuses.isEmpty()) ? DEFAULT_STATUSES : statuses;
        List<Invoice> invoices = search == null || search.isBlank()
                ? invoiceRepository.findAllWithFilters(dateFrom, dateTo, customerId, effectiveStatuses, returnStatus, dueStatus, productId, categoryId, orderBy, orderDir)
                : invoiceRepository.findAllWithFilters(dateFrom, dateTo, customerId, effectiveStatuses, search, returnStatus, dueStatus, productId, categoryId, orderBy, orderDir);

        List<UUID> invoiceIds = invoices.stream()
                .map(invoice -> invoice.getId().value())
                .toList();

        Map<UUID, BigDecimal> paidAmountByInvoiceId = invoiceIds.isEmpty()
                ? Map.of()
                : paymentAllocationRepository.sumByInvoiceIds(invoiceIds);

        Map<UUID, BigDecimal> debitAppliedByInvoiceId = debitNoteRepository.sumByInvoiceIds(invoiceIds);
        Map<UUID, BigDecimal> creditAppliedByInvoiceId = creditNoteRepository.sumByInvoiceIds(invoiceIds);

        return invoices.stream()
                .map(invoice -> InvoiceMapper.toListResponse(invoice, paidAmountByInvoiceId, debitAppliedByInvoiceId, creditAppliedByInvoiceId))
                .toList();
    }

    public List<InvoiceListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                              UUID customerId, List<String> statuses,
                                              List<String> returnStatus, UUID productId,
                                              UUID categoryId, String orderBy, String orderDir) {
        return execute(dateFrom, dateTo, customerId, statuses, null, returnStatus, null, productId,
                categoryId, orderBy, orderDir);
    }
}
