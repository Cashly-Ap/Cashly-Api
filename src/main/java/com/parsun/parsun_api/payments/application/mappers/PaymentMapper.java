package com.parsun.parsun_api.payments.application.mappers;

import com.parsun.parsun_api.payments.application.dto.response.PaymentListResponse;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;
import com.parsun.parsun_api.payments.domain.entities.Payment;

public class PaymentMapper {

    private PaymentMapper() {}

    public static PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId().value(),
                payment.getPaymentNumber().value(),
                payment.getCustomerId(),
                payment.getType().name(),
                payment.getAmount(),
                payment.getPaymentMethodId(),
                payment.getReferenceNumber(),
                payment.getPaymentDate(),
                payment.getNotes(),
                payment.getStatus().name(),
                payment.getAppliedAmount(),
                payment.getCreatedBy(),
                payment.getCreatedByName(),
                payment.getApprovalStatus(),
                payment.getRejectionReason(),
                payment.getApprovedBy(),
                payment.getCommissionPercentage(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    public static PaymentListResponse toListResponse(Payment payment) {
        return new PaymentListResponse(
                payment.getId().value(),
                payment.getPaymentNumber().value(),
                payment.getCustomerId(),
                payment.getType().name(),
                payment.getAmount(),
                payment.getReferenceNumber(),
                payment.getPaymentDate(),
                payment.getStatus().name(),
                payment.getAppliedAmount(),
                payment.getCreatedAt()
        );
    }
}
