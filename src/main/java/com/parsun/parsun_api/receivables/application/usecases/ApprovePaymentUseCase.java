package com.parsun.parsun_api.receivables.application.usecases;

import com.parsun.parsun_api.invoices.domain.services.InvoiceStatusService;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentId;
import com.parsun.parsun_api.receivables.application.dto.request.ApprovePaymentRequest;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class ApprovePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final InvoiceStatusService invoiceStatusService;

    public ApprovePaymentUseCase(PaymentRepository paymentRepository,
                                 PaymentAllocationRepository paymentAllocationRepository,
                                 InvoiceStatusService invoiceStatusService) {
        this.paymentRepository = paymentRepository;
        this.paymentAllocationRepository = paymentAllocationRepository;
        this.invoiceStatusService = invoiceStatusService;
    }

    @Transactional
    public void execute(UUID paymentId, ApprovePaymentRequest request, UUID authenticatedUserId) {
        Payment payment = paymentRepository.findById(PaymentId.of(paymentId))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        payment.approve(authenticatedUserId, request.commissionPercentage());
        paymentRepository.save(payment);

        paymentAllocationRepository.findByPaymentId(paymentId).stream()
                .map(a -> a.getInvoiceId())
                .distinct()
                .forEach(invoiceStatusService::recalculate);
    }
}
