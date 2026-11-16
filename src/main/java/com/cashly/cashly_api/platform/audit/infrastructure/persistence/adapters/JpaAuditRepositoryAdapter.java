package com.cashly.cashly_api.platform.audit.infrastructure.persistence.adapters;

import com.cashly.cashly_api.platform.audit.domain.entity.AuditEvent;
import com.cashly.cashly_api.platform.audit.domain.repository.AuditEventRepository;
import com.cashly.cashly_api.platform.audit.infrastructure.persistence.entities.AuditEventEntity;
import com.cashly.cashly_api.platform.audit.infrastructure.persistence.mappers.AuditJpaMapper;
import com.cashly.cashly_api.platform.audit.infrastructure.persistence.repositories.SpringDataAuditEventRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaAuditRepositoryAdapter implements AuditEventRepository {

    private final SpringDataAuditEventRepository repository;
    private final AuditJpaMapper mapper;

    public JpaAuditRepositoryAdapter(
            SpringDataAuditEventRepository repository, AuditJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public AuditEvent save(AuditEvent event) {
        AuditEventEntity entity = mapper.toEntity(event);
        AuditEventEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
}
