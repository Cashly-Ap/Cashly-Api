package com.cashly.cashly_api.platform.audit.infrastructure.persistence.mappers;

import com.cashly.cashly_api.platform.audit.domain.entity.AuditEvent;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditAction;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditEntityType;
import com.cashly.cashly_api.platform.audit.infrastructure.persistence.entities.AuditEventEntity;

public class AuditJpaMapper {

    public AuditEventEntity toEntity(AuditEvent event) {
        AuditEventEntity entity = new AuditEventEntity();
        entity.setId(event.id());
        entity.setCompanyId(event.companyId());
        entity.setModule(event.module());
        entity.setEntityType(event.entityType().name());
        entity.setEntityId(event.entityId());
        entity.setEntityLabel(event.entityLabel());
        entity.setEntitySecondaryLabel(event.entitySecondaryLabel());
        entity.setAction(event.action().name());
        entity.setActorId(event.actorId());
        entity.setActorName(event.actorName());
        entity.setOccurredAt(event.occurredAt());
        entity.setDescriptionKey(event.descriptionKey());
        entity.setDescriptionArgs(event.descriptionArgs());
        entity.setMetadata(event.metadata());
        entity.setSource(event.source());
        return entity;
    }

    public AuditEvent toDomain(AuditEventEntity entity) {
        return new AuditEvent(entity.getId(), entity.getCompanyId(),
                entity.getModule(), AuditEntityType.valueOf(entity.getEntityType()),
                entity.getEntityId(), entity.getEntityLabel(),
                entity.getEntitySecondaryLabel(),
                AuditAction.valueOf(entity.getAction()), entity.getActorId(),
                entity.getActorName(), null, entity.getOccurredAt(),
                entity.getDescriptionKey(), entity.getDescriptionArgs(),
                entity.getMetadata(), entity.getSource());
    }
}
