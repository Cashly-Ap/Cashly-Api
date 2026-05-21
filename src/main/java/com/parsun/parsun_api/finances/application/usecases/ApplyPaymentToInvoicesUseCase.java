package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.request.ApplyPaymentRequest;
import com.parsun.parsun_api.finances.application.dto.response.PaymentDetailResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.Payment;
import com.parsun.parsun_api.finances.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.finances.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.finances.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.finances.domain.services.PaymentAllocationService;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentId;
import com.parsun.parsun_api.finances.infrastructure.persistence.entities.PaymentMethodEntity;
import com.parsun.parsun_api.finances.infrastructure.persistence.repositories.PaymentMethodJpaRepository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

public class ApplyPaymentToInvoicesUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final PaymentAllocationService allocationService;
    private final PaymentMethodJpaRepository paymentMethodJpaRepository;

    public ApplyPaymentToInvoicesUseCase(PaymentRepository paymentRepository,
                                         PaymentAllocationRepository allocationRepository,
                                         PaymentAllocationService allocationService,
                                         PaymentMethodJpaRepository paymentMethodJpaRepository) {
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
        this.allocationService = allocationService;
        this.paymentMethodJpaRepository = paymentMethodJpaRepository;
    }

    @Transactional
    public PaymentDetailResponse execute(UUID paymentId, ApplyPaymentRequest request) {
        Payment payment = paymentRepository.findById(PaymentId.of(paymentId))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        List<PaymentAllocationService.AllocationRequest> allocations = request.allocations().stream()
                .map(entry -> new PaymentAllocationService.AllocationRequest(entry.invoiceId(), entry.amount()))
                .toList();

        allocationService.applyPaymentToInvoices(paymentId, allocations);

        Payment updated = paymentRepository.findById(PaymentId.of(paymentId))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found after update: " + paymentId));

        List<PaymentAllocation> paymentAllocations = allocationRepository.findByPaymentId(paymentId);
        String paymentMethodName = paymentMethodJpaRepository.findById(updated.getPaymentMethodId())
                .map(PaymentMethodEntity::getName)
                .orElse(null);

        return FinanceMapper.toPaymentDetailResponse(updated, paymentAllocations, paymentMethodName, null);
    }
}
