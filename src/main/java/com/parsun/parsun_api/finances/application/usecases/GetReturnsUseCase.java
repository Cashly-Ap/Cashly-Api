package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.ReturnListResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.Return;
import com.parsun.parsun_api.finances.domain.repositories.ReturnRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.ReturnType;
import com.parsun.parsun_api.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.parsun.parsun_api.invoices.infrastructure.persistence.repositories.InvoiceJpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetReturnsUseCase {

    private final ReturnRepository returnRepository;
    private final InvoiceJpaRepository invoiceJpaRepository;

    public GetReturnsUseCase(ReturnRepository returnRepository,
                             InvoiceJpaRepository invoiceJpaRepository) {
        this.returnRepository = returnRepository;
        this.invoiceJpaRepository = invoiceJpaRepository;
    }

    public List<ReturnListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                             UUID customerId, String returnType) {
        ReturnType type = returnType != null ? ReturnType.valueOf(returnType) : null;
        List<Return> returns = returnRepository.findAllWithFilters(dateFrom, dateTo, customerId, type);

        List<UUID> invoiceIds = returns.stream()
                .map(Return::getInvoiceId)
                .distinct()
                .toList();

        Map<UUID, InvoiceEntity> invoices = invoiceJpaRepository.findAllById(invoiceIds).stream()
                .collect(Collectors.toMap(InvoiceEntity::getId, inv -> inv));

        return returns.stream()
                .map(returnEntity -> {
                    InvoiceEntity invoice = invoices.get(returnEntity.getInvoiceId());
                    String invoiceNumber = invoice != null ? invoice.getInvoiceNumber() : null;
                    String customerName = invoice != null ? invoice.getCustomerName() : null;
                    return FinanceMapper.toReturnListResponse(returnEntity, invoiceNumber, customerName);
                })
                .toList();
    }
}
