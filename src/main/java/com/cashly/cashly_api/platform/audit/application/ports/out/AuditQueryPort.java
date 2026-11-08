package com.cashly.cashly_api.platform.audit.application.ports.out;

import com.cashly.cashly_api.platform.audit.domain.entity.AuditEvent;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditAction;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditEntityType;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditFilter;
import com.cashly.cashly_api.shared.dto.PageResponse;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditQueryPort {

    PageResponse<AuditEvent> search(AuditFilter filter);

    Optional<AuditEvent> findById(UUID id);

    AuditFilterOptions filterOptions(AuditEntityType entityType, UUID entityId,
            String module);

    record AuditFilterOptions(List<ActionCount> actions,
            List<ActorCount> actors, DateRange dateRange) {
    }

    record ActionCount(AuditAction action, long count) {
    }

    record ActorCount(UUID actorId, String actorName, String photoUrl,
            long count) {
    }

    record DateRange(Instant min, Instant max) {
    }
}
