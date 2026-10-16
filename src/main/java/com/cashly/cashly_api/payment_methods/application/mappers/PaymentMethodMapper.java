package com.cashly.cashly_api.payment_methods.application.mappers;

import com.cashly.cashly_api.payment_methods.application.dto.response.PaymentMethodResponse;
import com.cashly.cashly_api.payment_methods.domain.entity.PaymentMethod;

import java.util.List;

public class PaymentMethodMapper {

    private PaymentMethodMapper() {}

    public static PaymentMethodResponse toResponse(PaymentMethod paymentMethod, boolean hasAssociatedRecords) {
        return new PaymentMethodResponse(
                paymentMethod.getId().value(),
                paymentMethod.getCode(),
                paymentMethod.getName(),
                paymentMethod.getDescription(),
                paymentMethod.isActive(),
                hasAssociatedRecords,
                paymentMethod.getCreatedAt(),
                paymentMethod.getUpdatedAt()
        );
    }

    public static List<PaymentMethodResponse> toResponseList(List<PaymentMethod> paymentMethods, java.util.function.Function<PaymentMethod, Boolean> hasAssociatedRecords) {
        return paymentMethods.stream()
                .map(pm -> toResponse(pm, hasAssociatedRecords.apply(pm)))
                .toList();
    }
}
