package com.cashly.cashly_api.finance.treasury.payments.collection.application.mappers;

import com.cashly.cashly_api.finance.treasury.payments.collection.application.dto.response.PaymentListResponse;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.dto.response.PaymentResponse;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;

public class PaymentMapper {

    private PaymentMapper() {
    }

    public static PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(payment.getId().value(),
                payment.getPaymentNumber().value(), payment.getCustomerId(),
                payment.getAmount(), payment.getPaymentMethodId(),
                payment.getReferenceNumber(), payment.getPaymentDate(),
                payment.getNotes(), payment.getStatus().name(),
                payment.getAppliedAmount(), payment.getCreatedBy(),
                payment.getCreatedByName(), payment.getCreatedAt(),
                payment.getUpdatedAt(), payment.getCancelledReason(),
                payment.getVerifiedBy(), payment.getVerifiedByName(),
                payment.getVerifiedAt());
    }

    public static PaymentListResponse toListResponse(Payment payment) {
        return new PaymentListResponse(payment.getId().value(),
                payment.getPaymentNumber().value(), payment.getCustomerId(),
                payment.getAmount(), payment.getPaymentMethodId(),
                payment.getReferenceNumber(), payment.getPaymentDate(),
                payment.getStatus().name(), payment.getAppliedAmount(),
                payment.getCreatedAt());
    }
}
