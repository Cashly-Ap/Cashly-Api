package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceListResponse;
import com.parsun.parsun_api.invoices.application.mappers.InvoiceMapper;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GetInvoicesUseCase {

    private final InvoiceRepository invoiceRepository;

    public GetInvoicesUseCase(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public List<InvoiceListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                              UUID customerId, String status,
                                              UUID productId, UUID categoryId,
                                              String orderBy, String orderDir) {
        return invoiceRepository.findAllWithFilters(dateFrom, dateTo, customerId, status, productId, categoryId, orderBy, orderDir)
                .stream()
                .map(InvoiceMapper::toListResponse)
                .toList();
    }
}
