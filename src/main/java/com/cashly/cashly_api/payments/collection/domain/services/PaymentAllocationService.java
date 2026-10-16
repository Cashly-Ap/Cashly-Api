package com.cashly.cashly_api.payments.collection.domain.services;

import com.cashly.cashly_api.payments.collection.domain.entity.PaymentAllocation;
import com.cashly.cashly_api.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.InvoiceRemainingBalance;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.PaymentId;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.PaymentStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    public void cancelPayment(UUID paymentId, String reason) {
        Payment payment = paymentRepository.findById(PaymentId.of(paymentId))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        allocationRepository.deleteByPaymentId(paymentId);
        payment.cancel(reason);
        paymentRepository.save(payment);
    }

    @Deprecated
    public void cancelPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(PaymentId.of(paymentId))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        allocationRepository.deleteByPaymentId(paymentId);
        payment.cancel();
        paymentRepository.save(payment);
    }

    public void applyAdvanceToInvoice(UUID advanceId, UUID invoiceId, BigDecimal amount) {
        Payment advance = paymentRepository.findById(PaymentId.of(advanceId))
                .orElseThrow(() -> new IllegalArgumentException("Advance not found: " + advanceId));

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

    public List<PaymentAllocation> allocateOldestFirst(UUID paymentId,
                                                          List<InvoiceRemainingBalance> invoicesOldestFirst,
                                                          BigDecimal amountToAllocate) {
        List<PaymentAllocation> allocations = new ArrayList<>();
        BigDecimal remaining = amountToAllocate;

        for (InvoiceRemainingBalance invoice : invoicesOldestFirst) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            if (invoice.remainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal allocatedAmount = remaining.min(invoice.remainingAmount());
            allocations.add(PaymentAllocation.create(paymentId, invoice.invoiceId(), allocatedAmount));
            remaining = remaining.subtract(allocatedAmount);
        }

        return allocations;
    }

    public record AllocationRequest(UUID invoiceId, BigDecimal amount) {}
}
