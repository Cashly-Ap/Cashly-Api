package com.cashly.cashly_api.payments.infrastructure.adapters.out.persistence.mappers;

import com.cashly.cashly_api.payments.domain.entities.PaymentAllocation;
import com.cashly.cashly_api.payments.infrastructure.adapters.out.persistence.entities.PaymentAllocationEntity;

public final class PaymentAllocationJpaMapper {

    private PaymentAllocationJpaMapper() {}

    public static PaymentAllocationEntity toEntity(PaymentAllocation domain) {
        if (domain == null) return null;
        PaymentAllocationEntity entity = new PaymentAllocationEntity();
        entity.setId(domain.getId());
        entity.setPaymentId(domain.getPaymentId());
        entity.setInvoiceId(domain.getInvoiceId());
        entity.setAllocatedAmount(domain.getAllocatedAmount());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    public static PaymentAllocation toDomain(PaymentAllocationEntity entity) {
        if (entity == null) return null;
        return PaymentAllocation.reconstitute(
                entity.getId(),
                entity.getPaymentId(),
                entity.getInvoiceId(),
                entity.getAllocatedAmount(),
                entity.getCreatedAt()
        );
    }
}
