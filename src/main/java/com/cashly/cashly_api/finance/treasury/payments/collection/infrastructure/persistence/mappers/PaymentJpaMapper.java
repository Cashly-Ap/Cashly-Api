package com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.persistence.mappers;

import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentId;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentNumber;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentSource;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentStatus;
import com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.persistence.entities.PaymentEntity;

public class PaymentJpaMapper {

    private PaymentJpaMapper() {
    }

    public static PaymentEntity toEntity(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.setId(payment.getId().value());
        entity.setPaymentNumber(payment.getPaymentNumber().value());
        entity.setCustomerId(payment.getCustomerId());
        entity.setAmount(payment.getAmount());
        entity.setPaymentMethodId(payment.getPaymentMethodId());
        entity.setSource(payment.getSource() != null
                ? payment.getSource().name()
                : PaymentSource.PAYMENT_METHOD.name());
        entity.setReferenceNumber(payment.getReferenceNumber());
        entity.setPaymentDate(payment.getPaymentDate());
        entity.setNotes(payment.getNotes());
        entity.setStatus(payment.getStatus().name());
        entity.setAppliedAmount(payment.getAppliedAmount());
        entity.setCreatedBy(payment.getCreatedBy());
        entity.setCreatedByName(payment.getCreatedByName());
        entity.setCreatedAt(payment.getCreatedAt());
        entity.setUpdatedAt(payment.getUpdatedAt());
        entity.setCancelledReason(payment.getCancelledReason());
        entity.setVerifiedBy(payment.getVerifiedBy());
        entity.setVerifiedByName(payment.getVerifiedByName());
        entity.setVerifiedAt(payment.getVerifiedAt());
        return entity;
    }

    public static Payment toDomain(PaymentEntity entity) {
        PaymentSource source = entity.getSource() != null
                ? PaymentSource.valueOf(entity.getSource())
                : PaymentSource.PAYMENT_METHOD;
        return Payment.reconstitute(PaymentId.of(entity.getId()),
                new PaymentNumber(entity.getPaymentNumber()),
                entity.getCustomerId(), entity.getAmount(),
                entity.getPaymentMethodId(), source,
                entity.getReferenceNumber(), entity.getPaymentDate(),
                entity.getNotes(), PaymentStatus.valueOf(entity.getStatus()),
                entity.getAppliedAmount(), entity.getCreatedBy(),
                entity.getCreatedByName(), entity.getCreatedAt(),
                entity.getUpdatedAt(), entity.getCancelledReason(),
                entity.getVerifiedBy(), entity.getVerifiedByName(),
                entity.getVerifiedAt());
    }
}
