package com.cashly.cashly_api.finance.billing.invoices.application.list.use_case;

import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceNoteDetailsGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoicePaymentTotalsGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.dto.response.InvoiceListResponse;
import com.cashly.cashly_api.finance.billing.invoices.application.mappers.InvoiceMapper;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import com.cashly.cashly_api.shared.domain.valueobjects.DocumentAction;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GetInvoicesUseCase {
    private static final List<String> DEFAULT_STATUSES = List.of("DRAFT",
            "PENDING_PAYMENT", "PARTIALLY_PAID");

    private final InvoiceRepository invoiceRepository;
    private final InvoicePaymentTotalsGateway paymentTotals;
    private final InvoiceNoteDetailsGateway noteDetails;

    public GetInvoicesUseCase(InvoiceRepository invoiceRepository,
            InvoicePaymentTotalsGateway paymentTotals,
            InvoiceNoteDetailsGateway noteDetails) {
        this.invoiceRepository = invoiceRepository;
        this.paymentTotals = paymentTotals;
        this.noteDetails = noteDetails;
    }

    public List<InvoiceListResponse> execute(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, List<String> statuses,
            String search, List<String> returnStatus, String dueStatus,
            UUID productId, UUID categoryId, String orderBy, String orderDir) {
        List<String> effectiveStatuses = statuses == null || statuses.isEmpty()
                ? DEFAULT_STATUSES
                : statuses;
        List<Invoice> invoices = search == null || search.isBlank()
                ? invoiceRepository.findAllWithFilters(dateFrom, dateTo,
                        customerId, effectiveStatuses, returnStatus, dueStatus,
                        productId, categoryId, orderBy, orderDir)
                : invoiceRepository.findAllWithFilters(dateFrom, dateTo,
                        customerId, effectiveStatuses, search, returnStatus,
                        dueStatus, productId, categoryId, orderBy, orderDir);
        List<UUID> invoiceIds = invoices.stream()
                .map(invoice -> invoice.getId().value()).toList();
        Map<UUID, BigDecimal> paid = invoiceIds.isEmpty()
                ? Map.of()
                : paymentTotals.sumByInvoiceIds(invoiceIds);
        Map<UUID, BigDecimal> debits = noteDetails.sumDebitNotes(invoiceIds);
        Map<UUID, BigDecimal> credits = noteDetails.sumCreditNotes(invoiceIds);
        return invoices.stream().map(invoice -> InvoiceMapper
                .toListResponse(invoice, paid, debits, credits)).toList();
    }

    public List<InvoiceListResponse> execute(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, List<String> statuses,
            List<String> returnStatus, UUID productId, UUID categoryId,
            String orderBy, String orderDir) {
        return execute(dateFrom, dateTo, customerId, statuses, null,
                returnStatus, null, productId, categoryId, orderBy, orderDir);
    }
    public List<InvoiceListResponse> execute(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, List<String> statuses,
            String search, List<String> returnStatus, String dueStatus,
            UUID productId, UUID categoryId, String orderBy, String orderDir,
            DocumentAction action) {
        if (action == null)
            return execute(dateFrom, dateTo, customerId, statuses, search,
                    returnStatus, dueStatus, productId, categoryId, orderBy,
                    orderDir);
        List<String> eligibleStatuses = switch (action) {
            case EDIT, CONFIRM, DELETE -> List.of(InvoiceStatus.DRAFT.name());
            case CANCEL -> List.of(InvoiceStatus.PENDING_PAYMENT.name(),
                    InvoiceStatus.PARTIALLY_PAID.name());
            case DUPLICATE -> java.util.Arrays.stream(InvoiceStatus.values())
                    .map(Enum::name).toList();
            default -> throw new IllegalArgumentException(
                    "Unsupported invoice action: " + action);
        };
        List<String> effectiveStatuses = statuses == null || statuses.isEmpty()
                || statuses.contains("ALL")
                        ? eligibleStatuses
                        : eligibleStatuses.stream().filter(statuses::contains)
                                .toList();
        if (effectiveStatuses.isEmpty())
            return List.of();
        return execute(dateFrom, dateTo, customerId, effectiveStatuses, search,
                returnStatus, dueStatus, productId, categoryId, orderBy,
                orderDir);
    }

}
