package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceListResponse;
import com.parsun.parsun_api.invoices.application.mappers.InvoiceMapper;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.payments.infrastructure.persistence.repositories.PaymentAllocationJpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetInvoicesUseCase {

    private static final List<String> DEFAULT_STATUSES = List.of("DRAFT", "PENDING_PAYMENT", "PARTIALLY_PAID");

    private final InvoiceRepository invoiceRepository;
    private final PaymentAllocationJpaRepository paymentAllocationJpaRepository;

    public GetInvoicesUseCase(InvoiceRepository invoiceRepository,
                              PaymentAllocationJpaRepository paymentAllocationJpaRepository) {
        this.invoiceRepository = invoiceRepository;
        this.paymentAllocationJpaRepository = paymentAllocationJpaRepository;
    }

    public List<InvoiceListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                              UUID customerId, List<String> statuses,
                                              List<String> returnStatus,
                                              UUID productId, UUID categoryId,
                                              String orderBy, String orderDir) {
        List<String> effectiveStatuses = (statuses == null || statuses.isEmpty()) ? DEFAULT_STATUSES : statuses;
        List<Invoice> invoices = invoiceRepository.findAllWithFilters(dateFrom, dateTo, customerId, effectiveStatuses, returnStatus, productId, categoryId, orderBy, orderDir);

        Map<UUID, BigDecimal> paidAmountByInvoiceId = fetchPaidAmounts(invoices);

        return invoices.stream()
                .map(invoice -> InvoiceMapper.toListResponse(invoice, paidAmountByInvoiceId))
                .toList();
    }

    private Map<UUID, BigDecimal> fetchPaidAmounts(List<Invoice> invoices) {
        List<UUID> invoiceIds = invoices.stream()
                .map(invoice -> invoice.getId().value())
                .toList();

        if (invoiceIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> results = paymentAllocationJpaRepository.sumByInvoiceIds(invoiceIds);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (BigDecimal) row[1]
                ));
    }
}
