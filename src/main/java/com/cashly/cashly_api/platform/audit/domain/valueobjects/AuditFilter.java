package com.cashly.cashly_api.platform.audit.domain.valueobjects;

import com.cashly.cashly_api.platform.audit.domain.exceptions.AuditFilterException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

public record AuditFilter(
        AuditEntityType entityType,
        UUID entityId,
        String module,
        Set<AuditAction> actions,
        UUID actorId,
        LocalDate from,
        LocalDate to,
        int page,
        int size) {

    public static final int MAX_PAGE_SIZE = 200;

    public AuditFilter {
        String normalizedModule = module == null ? null : module.trim();
        if (entityType == null
                && (normalizedModule == null || normalizedModule.isEmpty())) {
            throw new AuditFilterException("audit.errors.entityRequired");
        }
        if (entityType != null && entityId == null) {
            throw new AuditFilterException("audit.errors.entityRequired");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new AuditFilterException("audit.errors.invalidRange");
        }
        if (page < 0) {
            throw new AuditFilterException("audit.errors.invalidPage");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new AuditFilterException("audit.errors.invalidPageSize",
                    MAX_PAGE_SIZE);
        }
        actions = actions == null ? Set.of() : Set.copyOf(actions);
        module = normalizedModule;
    }

    public Instant fromInstant() {
        return from == null
                ? null
                : from.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    public Instant toExclusiveInstant() {
        return to == null
                ? null
                : to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}
