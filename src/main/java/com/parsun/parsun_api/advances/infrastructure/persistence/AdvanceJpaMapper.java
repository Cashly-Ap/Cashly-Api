package com.parsun.parsun_api.advances.infrastructure.persistence;

import com.parsun.parsun_api.advances.domain.entities.Advance;
import com.parsun.parsun_api.advances.domain.valueobjects.AdvanceId;
import com.parsun.parsun_api.advances.domain.valueobjects.AdvanceNumber;
import com.parsun.parsun_api.advances.domain.valueobjects.AdvanceStatus;
import com.parsun.parsun_api.advances.infrastructure.persistence.entities.AdvanceEntity;

public class AdvanceJpaMapper {

    private AdvanceJpaMapper() {}

    public static AdvanceEntity toEntity(Advance domain) {
        AdvanceEntity entity = new AdvanceEntity();
        entity.setId(domain.getId().value());
        entity.setAdvanceNumber(domain.getAdvanceNumber().value());
        entity.setCustomerId(domain.getCustomerId());
        entity.setAmount(domain.getAmount());
        entity.setRemainingAmount(domain.getRemainingAmount());
        entity.setStatus(domain.getStatus().name());
        entity.setNotes(domain.getNotes());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public static Advance toDomain(AdvanceEntity entity) {
        return Advance.reconstitute(
                AdvanceId.of(entity.getId()),
                new AdvanceNumber(entity.getAdvanceNumber()),
                entity.getCustomerId(),
                entity.getAmount(),
                entity.getRemainingAmount(),
                AdvanceStatus.valueOf(entity.getStatus()),
                entity.getNotes(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
