package com.cashly.cashly_api.platform.audit.infrastructure.persistence.queries;

import com.cashly.cashly_api.platform.audit.application.ports.out.AuditQueryPort;
import com.cashly.cashly_api.platform.audit.domain.entity.AuditEvent;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditAction;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditEntityType;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditFilter;
import com.cashly.cashly_api.platform.audit.infrastructure.persistence.entities.AuditEventEntity;
import com.cashly.cashly_api.platform.audit.infrastructure.persistence.mappers.AuditJpaMapper;
import com.cashly.cashly_api.platform.company.domain.entity.Company;
import com.cashly.cashly_api.shared.dto.PageResponse;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaAuditQuery implements AuditQueryPort {

    private static final UUID COMPANY_ID = Company.INSTALLATION_ID;

    private final EntityManager entityManager;
    private final AuditJpaMapper mapper;

    public JpaAuditQuery(EntityManager entityManager, AuditJpaMapper mapper) {
        this.entityManager = entityManager;
        this.mapper = mapper;
    }

    @Override
    public PageResponse<AuditEvent> search(AuditFilter filter) {
        QueryParts parts = filters(filter);
        var query = entityManager.createQuery(
                "select e from AuditEventEntity e where " + parts.where()
                        + " order by e.occurredAt desc",
                AuditEventEntity.class);
        parts.params().forEach(query::setParameter);
        query.setFirstResult(filter.page() * filter.size());
        query.setMaxResults(filter.size());
        List<AuditEventEntity> events = query.getResultList();
        long total = count(parts);
        List<AuditEvent> content = events.stream().map(mapper::toDomain).toList();
        int totalPages = filter.size() == 0
                ? 0
                : (int) Math.ceil((double) total / filter.size());
        boolean first = filter.page() == 0;
        boolean last = totalPages == 0 || filter.page() + 1 >= totalPages;
        return new PageResponse<>(content, total, totalPages, filter.size(),
                filter.page(), first, last, content.isEmpty());
    }

    @Override
    public Optional<AuditEvent> findById(UUID id) {
        AuditEventEntity entity = entityManager.find(AuditEventEntity.class, id);
        if (entity == null || !COMPANY_ID.equals(entity.getCompanyId())) {
            return Optional.empty();
        }
        return Optional.of(mapper.toDomain(entity));
    }

    @Override
    public AuditFilterOptions filterOptions(AuditEntityType entityType,
            UUID entityId, String module) {
        QueryParts parts = scope(entityType, entityId, module);
        var actionsQuery = entityManager.createQuery(
                "select e.action, count(e) from AuditEventEntity e"
                        + " where " + parts.where()
                        + " group by e.action order by e.action",
                Object[].class);
        parts.params().forEach(actionsQuery::setParameter);
        List<ActionCount> actions = actionsQuery.getResultList().stream()
                .map(row -> new ActionCount(
                        AuditAction.valueOf((String) row[0]),
                        ((Number) row[1]).longValue()))
                .toList();
        var actorsQuery = entityManager.createQuery(
                "select e.actorId, e.actorName, count(e)"
                        + " from AuditEventEntity e where " + parts.where()
                        + " and e.actorId is not null"
                        + " group by e.actorId, e.actorName"
                        + " order by e.actorName", Object[].class);
        parts.params().forEach(actorsQuery::setParameter);
        List<ActorCount> actors = actorsQuery.getResultList().stream()
                .map(row -> new ActorCount((UUID) row[0], (String) row[1], null,
                        ((Number) row[2]).longValue()))
                .toList();
        var rangeQuery = entityManager.createQuery(
                "select min(e.occurredAt), max(e.occurredAt)"
                        + " from AuditEventEntity e where " + parts.where(),
                Object[].class);
        parts.params().forEach(rangeQuery::setParameter);
        Object[] range = rangeQuery.getSingleResult();
        DateRange dateRange = new DateRange(instant(range[0]), instant(range[1]));
        return new AuditFilterOptions(actions, actors, dateRange);
    }

    private long count(QueryParts parts) {
        var query = entityManager.createQuery(
                "select count(e) from AuditEventEntity e where "
                        + parts.where(),
                Long.class);
        parts.params().forEach(query::setParameter);
        return query.getSingleResult();
    }

    private QueryParts filters(AuditFilter filter) {
        QueryParts scope = scope(filter.entityType(), filter.entityId(),
                filter.module());
        var where = new StringBuilder(scope.where());
        var params = new HashMap<>(scope.params());
        if (!filter.actions().isEmpty()) {
            where.append(" and e.action in :actions");
            params.put("actions", filter.actions().stream()
                    .map(AuditAction::name).toList());
        }
        if (filter.actorId() != null) {
            where.append(" and e.actorId = :actorId");
            params.put("actorId", filter.actorId());
        }
        if (filter.from() != null) {
            where.append(" and e.occurredAt >= :from");
            params.put("from", filter.fromInstant());
        }
        if (filter.to() != null) {
            where.append(" and e.occurredAt < :to");
            params.put("to", filter.toExclusiveInstant());
        }
        return new QueryParts(where.toString(), params);
    }

    private QueryParts scope(AuditEntityType entityType, UUID entityId,
            String module) {
        var where = new StringBuilder("e.companyId = :companyId");
        var params = new HashMap<String, Object>();
        params.put("companyId", COMPANY_ID);
        if (entityType != null) {
            where.append(" and e.entityType = :entityType")
                    .append(" and e.entityId = :entityId");
            params.put("entityType", entityType.name());
            params.put("entityId", entityId);
        }
        if (module != null && !module.isBlank()) {
            where.append(" and e.module = :module");
            params.put("module", module.trim());
        }
        return new QueryParts(where.toString(), params);
    }

    private Instant instant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toInstant();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toInstant(ZoneOffset.UTC);
        }
        return null;
    }

    private record QueryParts(String where, Map<String, Object> params) {
    }
}
