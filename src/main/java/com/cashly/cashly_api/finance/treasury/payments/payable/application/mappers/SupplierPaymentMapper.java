package com.cashly.cashly_api.finance.treasury.payments.payable.application.mappers;

import com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.response.SupplierPaymentResponse;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.entity.SupplierPayment;

public class SupplierPaymentMapper {

    private SupplierPaymentMapper() {
    }

    public static SupplierPaymentResponse toResponse(SupplierPayment payment) {
        return new SupplierPaymentResponse(payment.getId().value(),
                payment.getPaymentNumber().value(), payment.getSupplierId(),
                payment.getAmount(), payment.getAppliedAmount(),
                payment.getPaymentMethodId(), payment.getSource().name(),
                payment.getReferenceNumber(), payment.getPaymentDate(),
                payment.getNotes(), payment.getStatus().name(),
                payment.getCreatedAt());
    }
}
