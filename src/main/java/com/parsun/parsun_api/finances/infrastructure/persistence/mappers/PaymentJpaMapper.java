package com.parsun.parsun_api.finances.infrastructure.persistence.mappers;

import com.parsun.parsun_api.finances.domain.entities.Payment;
import com.parsun.parsun_api.finances.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentId;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentNumber;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentStatus;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.finances.infrastructure.persistence.entities.PaymentAllocationEntity;
import com.parsun.parsun_api.finances.infrastructure.persistence.entities.PaymentEntity;

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
        entity.setCreatedAt(payment.getCreatedAt());
        entity.setUpdatedAt(payment.getUpdatedAt());
        return entity;
    }

    public static Payment toDomain(PaymentEntity entity) {
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

    public static PaymentAllocationEntity toAllocationEntity(PaymentAllocation allocation) {
        PaymentAllocationEntity entity = new PaymentAllocationEntity();
        entity.setId(allocation.getId());
        entity.setPaymentId(allocation.getPaymentId());
        entity.setInvoiceId(allocation.getInvoiceId());
        entity.setAllocatedAmount(allocation.getAllocatedAmount());
        entity.setCreatedAt(allocation.getCreatedAt());
        return entity;
    }

    public static PaymentAllocation toAllocationDomain(PaymentAllocationEntity entity) {
        return PaymentAllocation.reconstitute(
                entity.getId(),
                entity.getPaymentId(),
                entity.getInvoiceId(),
                entity.getAllocatedAmount(),
                entity.getCreatedAt()
        );
    }
}
