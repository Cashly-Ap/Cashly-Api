package com.parsun.parsun_api.portal.application.usecase;

import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.payment_methods.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.payment_methods.domain.valueobjects.PaymentMethodId;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentId;
import com.parsun.parsun_api.portal.application.dto.response.PortalPaymentDetailResponse;
import com.parsun.parsun_api.portal.application.dto.response.PortalPaymentListResponse;
import com.parsun.parsun_api.portal.application.mapper.PortalPaymentMapper;

import java.util.List;
import java.util.UUID;

public class GetCustomerPaymentsUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final InvoiceRepository invoiceRepository;

    public GetCustomerPaymentsUseCase(PaymentRepository paymentRepository,
                                       PaymentMethodRepository paymentMethodRepository,
                                       PaymentAllocationRepository paymentAllocationRepository,
                                       InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.paymentAllocationRepository = paymentAllocationRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public List<PortalPaymentListResponse> execute(String customerId) {
        UUID customerUuid = UUID.fromString(customerId);
        return paymentRepository.findAllWithFilters(null, null, customerUuid, null)
            .stream()
            .map(payment -> {
                var paymentMethod = payment.getPaymentMethodId() != null
                    ? paymentMethodRepository.findById(PaymentMethodId.of(payment.getPaymentMethodId())).orElse(null)
                    : null;
                return PortalPaymentMapper.toListResponse(payment, paymentMethod);
            })
            .toList();
    }

    public PortalPaymentDetailResponse execute(String customerId, String paymentId) {
        UUID customerUuid = UUID.fromString(customerId);
        PaymentId paymentIdObj = PaymentId.of(UUID.fromString(paymentId));

        var payment = paymentRepository.findById(paymentIdObj)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (!payment.getCustomerId().equals(customerUuid)) {
            throw new IllegalArgumentException("Payment not found");
        }

        var paymentMethod = payment.getPaymentMethodId() != null
            ? paymentMethodRepository.findById(PaymentMethodId.of(payment.getPaymentMethodId())).orElse(null)
            : null;

        var allocations = paymentAllocationRepository.findByPaymentId(payment.getId().value());

        String invoiceNumber = null;
        if (!allocations.isEmpty()) {
            var firstInvoiceId = allocations.getFirst().getInvoiceId();
            invoiceNumber = invoiceRepository.findById(InvoiceId.of(firstInvoiceId))
                .map(inv -> inv.getInvoiceNumber().value())
                .orElse(null);
        }

        return PortalPaymentMapper.toDetailResponse(payment, paymentMethod, allocations, invoiceNumber);
    }
}
