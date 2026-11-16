package com.cashly.cashly_api.platform.audit.infrastructure.persistence.repositories;

import com.cashly.cashly_api.platform.audit.infrastructure.persistence.entities.AuditEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataAuditEventRepository
        extends
            JpaRepository<AuditEventEntity, UUID> {
}
