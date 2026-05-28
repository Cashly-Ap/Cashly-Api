package com.parsun.parsun_api.payment_methods.application.mappers;

import com.parsun.parsun_api.payment_methods.application.dto.response.PaymentMethodResponse;
import com.parsun.parsun_api.payment_methods.domain.entities.PaymentMethod;

import java.util.List;

public class PaymentMethodMapper {

    private PaymentMethodMapper() {}

    public static PaymentMethodResponse toResponse(PaymentMethod paymentMethod) {
        return new PaymentMethodResponse(
                paymentMethod.getId().value(),
                paymentMethod.getCode(),
                paymentMethod.getName(),
                paymentMethod.getDescription(),
                paymentMethod.isActive(),
                paymentMethod.getCreatedAt(),
                paymentMethod.getUpdatedAt()
        );
    }

    public static List<PaymentMethodResponse> toResponseList(List<PaymentMethod> paymentMethods) {
        return paymentMethods.stream()
                .map(PaymentMethodMapper::toResponse)
                .toList();
    }
}
