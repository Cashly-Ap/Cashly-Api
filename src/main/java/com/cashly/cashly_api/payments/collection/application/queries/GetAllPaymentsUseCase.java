package com.cashly.cashly_api.payments.collection.application.queries;

import com.cashly.cashly_api.payments.collection.application.dto.response.PaymentListResponse;
import com.cashly.cashly_api.payments.collection.application.mappers.PaymentMapper;
import com.cashly.cashly_api.payments.collection.domain.PaymentRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GetAllPaymentsUseCase {

    private final PaymentRepository paymentRepository;

    public GetAllPaymentsUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<PaymentListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                               UUID customerId, String type, String status) {
        String effectiveStatus = (type != null && status == null) ? type : status;
        return paymentRepository.findAllWithFilters(dateFrom, dateTo, customerId, effectiveStatus)
                .stream()
                .map(PaymentMapper::toListResponse)
                .toList();
    }
}
