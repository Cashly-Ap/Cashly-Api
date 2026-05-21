package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.PaymentListResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.Payment;
import com.parsun.parsun_api.finances.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.finances.infrastructure.persistence.entities.PaymentMethodEntity;
import com.parsun.parsun_api.finances.infrastructure.persistence.repositories.PaymentMethodJpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetPaymentsUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentMethodJpaRepository paymentMethodJpaRepository;

    public GetPaymentsUseCase(PaymentRepository paymentRepository,
                              PaymentMethodJpaRepository paymentMethodJpaRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentMethodJpaRepository = paymentMethodJpaRepository;
    }

    public List<PaymentListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                              UUID customerId, String type, String status) {
        List<Payment> payments = paymentRepository.findAllWithFilters(dateFrom, dateTo, customerId, type, status);

        List<UUID> methodIds = payments.stream()
                .map(Payment::getPaymentMethodId)
                .distinct()
                .toList();

        Map<UUID, String> methodNames = paymentMethodJpaRepository.findAllById(methodIds).stream()
                .collect(Collectors.toMap(PaymentMethodEntity::getId, PaymentMethodEntity::getName));

        return payments.stream()
                .map(payment -> FinanceMapper.toPaymentListResponse(payment, methodNames.get(payment.getPaymentMethodId())))
                .toList();
    }
}
