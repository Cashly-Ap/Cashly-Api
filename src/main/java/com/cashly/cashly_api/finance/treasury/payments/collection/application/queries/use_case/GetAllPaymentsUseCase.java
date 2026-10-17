package com.cashly.cashly_api.finance.treasury.payments.collection.application.queries.use_case;

import com.cashly.cashly_api.finance.treasury.payments.collection.application.dto.response.PaymentListResponse;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.mappers.PaymentMapper;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;

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
