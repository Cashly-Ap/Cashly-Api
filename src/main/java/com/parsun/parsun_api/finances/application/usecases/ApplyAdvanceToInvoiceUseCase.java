package com.parsun.parsun_api.finances.application.usecases;

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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ApplyAdvanceToInvoiceUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final PaymentAllocationService allocationService;
    private final PaymentMethodJpaRepository paymentMethodJpaRepository;

    public ApplyAdvanceToInvoiceUseCase(PaymentRepository paymentRepository,
                                        PaymentAllocationRepository allocationRepository,
                                        PaymentAllocationService allocationService,
                                        PaymentMethodJpaRepository paymentMethodJpaRepository) {
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
        this.allocationService = allocationService;
        this.paymentMethodJpaRepository = paymentMethodJpaRepository;
    }

    @Transactional
    public PaymentDetailResponse execute(UUID advanceId, UUID invoiceId, BigDecimal amount) {
        allocationService.applyAdvanceToInvoice(advanceId, invoiceId, amount);

        Payment updated = paymentRepository.findById(PaymentId.of(advanceId))
                .orElseThrow(() -> new IllegalArgumentException("Advance not found after update: " + advanceId));

        List<PaymentAllocation> allocations = allocationRepository.findByPaymentId(advanceId);
        String paymentMethodName = paymentMethodJpaRepository.findById(updated.getPaymentMethodId())
                .map(PaymentMethodEntity::getName)
                .orElse(null);

        return FinanceMapper.toPaymentDetailResponse(updated, allocations, paymentMethodName, null);
    }
}
