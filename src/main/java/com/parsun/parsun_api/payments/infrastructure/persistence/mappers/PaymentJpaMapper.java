package com.parsun.parsun_api.payments.infrastructure.persistence.mappers;

import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentId;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentNumber;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentStatus;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.payments.infrastructure.persistence.entities.PaymentEntity;

public class PaymentJpaMapper {

    private PaymentJpaMapper() {}

    public static PaymentEntity toEntity(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.setId(payment.getId().value());
        entity.setPaymentNumber(payment.getPaymentNumber().value());
        entity.setCustomerId(payment.getCustomerId());
        entity.setType(payment.getType().name());
        entity.setAmount(payment.getAmount());
        entity.setPaymentMethodId(payment.getPaymentMethodId());
        entity.setReferenceNumber(payment.getReferenceNumber());
        entity.setPaymentDate(payment.getPaymentDate());
        entity.setNotes(payment.getNotes());
        entity.setStatus(payment.getStatus().name());
        entity.setAppliedAmount(payment.getAppliedAmount());
        entity.setCreatedBy(payment.getCreatedBy());
        entity.setCreatedByName(payment.getCreatedByName());
        entity.setApprovalStatus(payment.getApprovalStatus());
        entity.setRejectionReason(payment.getRejectionReason());
        entity.setApprovedBy(payment.getApprovedBy());
        entity.setCommissionPercentage(payment.getCommissionPercentage());
        entity.setCreatedAt(payment.getCreatedAt());
        entity.setUpdatedAt(payment.getUpdatedAt());
        return entity;
    }

    public static Payment toDomain(PaymentEntity entity) {
        String approvalStatus = entity.getApprovalStatus();
        if (approvalStatus != null) {
            return Payment.reconstitute(
                    PaymentId.of(entity.getId()),
                    new PaymentNumber(entity.getPaymentNumber()),
                    entity.getCustomerId(),
                    PaymentType.valueOf(entity.getType()),
                    entity.getAmount(),
                    entity.getPaymentMethodId(),
                    entity.getReferenceNumber(),
                    entity.getPaymentDate(),
                    entity.getNotes(),
                    PaymentStatus.valueOf(entity.getStatus()),
                    entity.getAppliedAmount(),
                    entity.getCreatedBy(),
                    entity.getCreatedByName(),
                    entity.getCreatedAt(),
                    entity.getUpdatedAt(),
                    entity.getApprovalStatus(),
                    entity.getRejectionReason(),
                    entity.getApprovedBy(),
                    entity.getCommissionPercentage()
            );
        }

        return Payment.reconstitute(
                PaymentId.of(entity.getId()),
                new PaymentNumber(entity.getPaymentNumber()),
                entity.getCustomerId(),
                PaymentType.valueOf(entity.getType()),
                entity.getAmount(),
                entity.getPaymentMethodId(),
                entity.getReferenceNumber(),
                entity.getPaymentDate(),
                entity.getNotes(),
                PaymentStatus.valueOf(entity.getStatus()),
                entity.getAppliedAmount(),
                entity.getCreatedBy(),
                entity.getCreatedByName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
