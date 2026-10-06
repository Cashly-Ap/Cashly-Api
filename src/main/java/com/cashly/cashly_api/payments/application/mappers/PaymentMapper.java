package com.cashly.cashly_api.payments.application.mappers;

import com.cashly.cashly_api.payments.application.dto.response.PaymentListResponse;
import com.cashly.cashly_api.payments.application.dto.response.PaymentResponse;
import com.cashly.cashly_api.payments.domain.entities.Payment;

public class PaymentMapper {

    private static final String VOUCHER_URL_PREFIX = "/api/files/";

    private PaymentMapper() {}

    public static PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId().value(),
                payment.getPaymentNumber().value(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getPaymentMethodId(),
                payment.getReferenceNumber(),
                payment.getPaymentDate(),
                payment.getNotes(),
                payment.getStatus().name(),
                payment.getAppliedAmount(),
                payment.getCreatedBy(),
                payment.getCreatedByName(),
                payment.getCreatedAt(),
                payment.getUpdatedAt(),
                payment.getCancelledReason(),
                payment.getVerifiedBy(),
                payment.getVerifiedByName(),
                payment.getVerifiedAt(),
                payment.getTargetInvoiceId(),
                toVoucherUrl(payment.getVoucherPath())
        );
    }

    public static PaymentListResponse toListResponse(Payment payment) {
        return new PaymentListResponse(
                payment.getId().value(),
                payment.getPaymentNumber().value(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getPaymentMethodId(),
                payment.getReferenceNumber(),
                payment.getPaymentDate(),
                payment.getStatus().name(),
                payment.getAppliedAmount(),
                payment.getCreatedAt(),
                payment.getTargetInvoiceId(),
                toVoucherUrl(payment.getVoucherPath())
        );
    }

    private static String toVoucherUrl(String voucherPath) {
        return voucherPath == null ? null : VOUCHER_URL_PREFIX + voucherPath;
    }
}
