package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceListResponse;
import com.parsun.parsun_api.invoices.application.mappers.InvoiceMapper;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GetInvoicesUseCase {

    private static final List<String> DEFAULT_STATUSES = List.of("DRAFT", "PENDING_PAYMENT", "PARTIALLY_PAID");

    private final InvoiceRepository invoiceRepository;

    public GetInvoicesUseCase(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public List<InvoiceListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                              UUID customerId, List<String> statuses,
                                              List<String> returnStatus,
                                              UUID productId, UUID categoryId,
                                              String orderBy, String orderDir) {
        List<String> effectiveStatuses = (statuses == null || statuses.isEmpty()) ? DEFAULT_STATUSES : statuses;
        return invoiceRepository.findAllWithFilters(dateFrom, dateTo, customerId, effectiveStatuses, returnStatus, productId, categoryId, orderBy, orderDir)
                .stream()
                .map(InvoiceMapper::toListResponse)
                .toList();
    }
}
