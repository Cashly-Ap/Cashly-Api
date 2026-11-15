package com.cashly.cashly_api.finance.treasury.payments.payable.application.list.use_case;

import com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.response.SupplierPaymentResponse;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.mappers.SupplierPaymentMapper;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.repository.SupplierPaymentRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ListSupplierPaymentsUseCase {

    private final SupplierPaymentRepository payments;

    public ListSupplierPaymentsUseCase(SupplierPaymentRepository payments) {
        this.payments = payments;
    }

    public List<SupplierPaymentResponse> execute(LocalDate dateFrom,
            LocalDate dateTo, UUID supplierId, String status) {
        return payments.findAllWithFilters(dateFrom, dateTo, supplierId, status)
                .stream().map(SupplierPaymentMapper::toResponse).toList();
    }

    public PageResponse<SupplierPaymentResponse> executePage(
            LocalDate dateFrom, LocalDate dateTo, UUID supplierId,
            String status, int page, int size, String orderBy,
            String orderDir) {
        PageResponse<com.cashly.cashly_api.finance.treasury.payments.payable.domain.entity.SupplierPayment> result = payments
                .findPageWithFilters(dateFrom, dateTo, supplierId, status, page,
                        size, orderBy, orderDir);
        List<SupplierPaymentResponse> content = result.content().stream()
                .map(SupplierPaymentMapper::toResponse).toList();
        return new PageResponse<>(content, result.totalElements(),
                result.totalPages(), result.size(), result.number(),
                result.first(), result.last(), content.isEmpty());
    }
}
