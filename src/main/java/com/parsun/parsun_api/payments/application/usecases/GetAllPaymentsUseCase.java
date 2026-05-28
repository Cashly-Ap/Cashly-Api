package com.parsun.parsun_api.payments.application.usecases;

import com.parsun.parsun_api.payments.application.dto.response.PaymentListResponse;
import com.parsun.parsun_api.payments.application.mappers.PaymentMapper;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;

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
        return paymentRepository.findAllWithFilters(dateFrom, dateTo, customerId, type, status)
                .stream()
                .map(PaymentMapper::toListResponse)
                .toList();
    }
}
