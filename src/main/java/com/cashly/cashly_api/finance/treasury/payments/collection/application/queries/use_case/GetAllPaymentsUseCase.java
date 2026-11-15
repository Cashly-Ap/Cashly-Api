package com.cashly.cashly_api.finance.treasury.payments.collection.application.queries.use_case;

import com.cashly.cashly_api.finance.treasury.payments.collection.application.dto.response.PaymentListResponse;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.mappers.PaymentMapper;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GetAllPaymentsUseCase {

    private final PaymentRepository paymentRepository;

    public GetAllPaymentsUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public enum ListAction {
        APPROVE, REJECT
    }

    public List<PaymentListResponse> execute(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, String type, String status) {
        return execute(dateFrom, dateTo, customerId, type, status, null);
    }

    public List<PaymentListResponse> execute(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, String type, String status,
            ListAction action) {
        String effectiveStatus = action != null
                ? "PENDING"
                : (type != null && status == null) ? type : status;
        return paymentRepository
                .findAllWithFilters(dateFrom, dateTo, customerId,
                        effectiveStatus)
                .stream().map(PaymentMapper::toListResponse).toList();
    }

    public PageResponse<PaymentListResponse> executePage(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, String type, String status,
            ListAction action, int page, int size, String orderBy,
            String orderDir) {
        String effectiveStatus = action != null
                ? "PENDING"
                : (type != null && status == null) ? type : status;
        PageResponse<com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment> payments = paymentRepository
                .findPageWithFilters(dateFrom, dateTo, customerId,
                        effectiveStatus, page, size, orderBy, orderDir);
        List<PaymentListResponse> content = payments.content().stream()
                .map(PaymentMapper::toListResponse).toList();
        return new PageResponse<>(content, payments.totalElements(),
                payments.totalPages(), payments.size(), payments.number(),
                payments.first(), payments.last(), content.isEmpty());
    }
}
