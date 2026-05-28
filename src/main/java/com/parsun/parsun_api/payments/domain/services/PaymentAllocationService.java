package com.parsun.parsun_api.payments.domain.services;

import com.parsun.parsun_api.payments.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentId;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentStatus;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class PaymentAllocationService {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;

    public PaymentAllocationService(PaymentRepository paymentRepository,
                                    PaymentAllocationRepository allocationRepository) {
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
    }

    public void applyPaymentToInvoices(UUID paymentId, List<AllocationRequest> allocations) {
        Payment payment = paymentRepository.findById(PaymentId.of(paymentId))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot apply allocations to a cancelled payment");
        }

        BigDecimal totalAllocation = allocations.stream()
                .map(AllocationRequest::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = payment.getAmount().subtract(payment.getAppliedAmount());
        if (totalAllocation.compareTo(remaining) > 0) {
            throw new IllegalArgumentException("Total allocation exceeds remaining payment amount");
        }

        for (AllocationRequest req : allocations) {
            PaymentAllocation allocation = PaymentAllocation.create(paymentId, req.invoiceId(), req.amount());
            allocationRepository.save(allocation);
        }

        payment.apply(totalAllocation);
        paymentRepository.save(payment);
    }

    public void cancelPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(PaymentId.of(paymentId))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        if (payment.getStatus() == PaymentStatus.DRAFT) {
            throw new IllegalStateException("DRAFT payments cannot be cancelled");
        }

        allocationRepository.deleteByPaymentId(paymentId);
        payment.cancel();
        paymentRepository.save(payment);
    }

    public void applyAdvanceToInvoice(UUID advanceId, UUID invoiceId, BigDecimal amount) {
        Payment advance = paymentRepository.findById(PaymentId.of(advanceId))
                .orElseThrow(() -> new IllegalArgumentException("Advance not found: " + advanceId));

        if (advance.getType() != PaymentType.ADVANCE) {
            throw new IllegalArgumentException("Payment is not an advance");
        }

        if (advance.getStatus() == PaymentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot apply a cancelled advance");
        }

        BigDecimal remaining = advance.getAmount().subtract(advance.getAppliedAmount());
        if (amount.compareTo(remaining) > 0) {
            throw new IllegalArgumentException("Amount exceeds remaining advance balance");
        }

        PaymentAllocation allocation = PaymentAllocation.create(advanceId, invoiceId, amount);
        allocationRepository.save(allocation);

        advance.apply(amount);
        paymentRepository.save(advance);
    }

    public record AllocationRequest(UUID invoiceId, BigDecimal amount) {}
}
