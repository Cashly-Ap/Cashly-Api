package com.parsun.parsun_api.portal.application.mapper;

import com.parsun.parsun_api.payment_methods.domain.entities.PaymentMethod;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.portal.application.dto.response.PortalPaymentDetailResponse;
import com.parsun.parsun_api.portal.application.dto.response.PortalPaymentInvoiceResponse;
import com.parsun.parsun_api.portal.application.dto.response.PortalPaymentListResponse;

import java.util.List;

public final class PortalPaymentMapper {

    private PortalPaymentMapper() {}

    public static PortalPaymentListResponse toListResponse(Payment payment, PaymentMethod paymentMethod) {
        return new PortalPaymentListResponse(
            payment.getId().value(),
            payment.getPaymentNumber().value(),
            payment.getPaymentDate(),
            paymentMethod != null ? paymentMethod.getName() : "",
            payment.getStatus().name(),
            payment.getAmount(),
            payment.getReferenceNumber(),
            payment.getCreatedAt()
        );
    }

    public static PortalPaymentDetailResponse toDetailResponse(
            Payment payment,
            PaymentMethod paymentMethod,
            List<PaymentAllocation> allocations,
            String invoiceNumber
    ) {
        List<PortalPaymentInvoiceResponse> invoiceResponses = allocations.stream()
            .map(a -> new PortalPaymentInvoiceResponse(
                a.getInvoiceId(),
                invoiceNumber,
                a.getAllocatedAmount()
            ))
            .toList();

        return new PortalPaymentDetailResponse(
            payment.getId().value(),
            payment.getPaymentNumber().value(),
            payment.getPaymentDate(),
            paymentMethod != null ? paymentMethod.getName() : "",
            payment.getStatus().name(),
            payment.getAmount(),
            payment.getAppliedAmount(),
            payment.getReferenceNumber(),
            payment.getReferenceNumber(),
            payment.getNotes(),
            payment.getCreatedAt(),
            invoiceResponses
        );
    }
}
