package com.parsun.parsun_api.payments.application.usecases;

import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.services.InvoiceStatusService;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.notifications.application.services.NotificationService;
import com.parsun.parsun_api.payments.application.dto.request.ApprovePaymentRequest;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;
import com.parsun.parsun_api.payments.application.mappers.PaymentMapper;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentId;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ApprovePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceStatusService invoiceStatusService;
    private final NotificationService notificationService;

    public ApprovePaymentUseCase(PaymentRepository paymentRepository,
                                   PaymentAllocationRepository allocationRepository,
                                   InvoiceRepository invoiceRepository,
                                   InvoiceStatusService invoiceStatusService,
                                   NotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceStatusService = invoiceStatusService;
        this.notificationService = notificationService;
    }

    @Transactional
    public PaymentResponse execute(UUID id, ApprovePaymentRequest request, UUID userId, String userName) {
        Payment payment = paymentRepository.findById(PaymentId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));

        payment.approve(request.reason(), userId, userName);

        String notes = payment.getNotes();
        if (notes != null && notes.startsWith("INVOICE_ID:")) {
            String invoiceIdStr = notes.substring("INVOICE_ID:".length()).trim();
            applyToInvoice(payment, UUID.fromString(invoiceIdStr));
        }

        Payment saved = paymentRepository.save(payment);
        notificationService.notifyCustomerPaymentApproved(payment.getCustomerId(), id);
        return PaymentMapper.toResponse(saved);
    }

    private void applyToInvoice(Payment payment, UUID invoiceId) {
        invoiceRepository.findById(new InvoiceId(invoiceId))
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        PaymentAllocation allocation = PaymentAllocation.create(
                payment.getId().value(), invoiceId, payment.getAmount());
        allocationRepository.save(allocation);
        payment.apply(payment.getAmount());
        invoiceStatusService.recalculate(invoiceId);
    }
}
