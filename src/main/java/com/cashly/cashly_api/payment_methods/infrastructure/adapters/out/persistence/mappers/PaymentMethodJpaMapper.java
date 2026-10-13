package com.cashly.cashly_api.payment_methods.infrastructure.adapters.out.persistence.mappers;

import com.cashly.cashly_api.payment_methods.domain.entities.PaymentMethod;
import com.cashly.cashly_api.payment_methods.domain.valueobjects.PaymentMethodId;
import com.cashly.cashly_api.payment_methods.infrastructure.adapters.out.persistence.entities.PaymentMethodEntity;

public class PaymentMethodJpaMapper {

    private PaymentMethodJpaMapper() {}

    public static PaymentMethodEntity toEntity(PaymentMethod paymentMethod) {
        PaymentMethodEntity entity = new PaymentMethodEntity();
        entity.setId(paymentMethod.getId().value());
        entity.setCode(paymentMethod.getCode());
        entity.setName(paymentMethod.getName());
        entity.setDescription(paymentMethod.getDescription());
        entity.setActive(paymentMethod.isActive());
        entity.setCreatedAt(paymentMethod.getCreatedAt());
        entity.setUpdatedAt(paymentMethod.getUpdatedAt());
        return entity;
    }

    public static PaymentMethod toDomain(PaymentMethodEntity entity) {
        return PaymentMethod.reconstitute(
                PaymentMethodId.of(entity.getId()),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
