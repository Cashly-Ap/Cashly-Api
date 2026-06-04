package com.parsun.parsun_api.advances.infrastructure.persistence;

import com.parsun.parsun_api.advances.domain.entities.AdvanceConsumption;
import com.parsun.parsun_api.advances.infrastructure.persistence.entities.AdvanceConsumptionEntity;

public class AdvanceConsumptionJpaMapper {

    private AdvanceConsumptionJpaMapper() {}

    public static AdvanceConsumptionEntity toEntity(AdvanceConsumption domain) {
        AdvanceConsumptionEntity entity = new AdvanceConsumptionEntity();
        entity.setId(domain.getId());
        entity.setAdvanceId(domain.getAdvanceId());
        entity.setInvoiceId(domain.getInvoiceId());
        entity.setAmount(domain.getAmount());
        entity.setPaymentId(domain.getPaymentId());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    public static AdvanceConsumption toDomain(AdvanceConsumptionEntity entity) {
        return AdvanceConsumption.reconstitute(
                entity.getId(),
                entity.getAdvanceId(),
                entity.getInvoiceId(),
                entity.getAmount(),
                entity.getPaymentId(),
                entity.getCreatedAt()
        );
    }
}
