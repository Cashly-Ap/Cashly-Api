package com.cashly.cashly_api.platform.audit.domain.entity;

import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditAction;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditEntityType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record AuditEvent(
        UUID id,
        UUID companyId,
        String module,
        AuditEntityType entityType,
        UUID entityId,
        String entityLabel,
        String entitySecondaryLabel,
        AuditAction action,
        UUID actorId,
        String actorName,
        String actorPhotoUrl,
        Instant occurredAt,
        String descriptionKey,
        List<String> descriptionArgs,
        Map<String, Object> metadata,
        String source) {

    public AuditEvent {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(module, "module is required");
        Objects.requireNonNull(entityType, "entityType is required");
        Objects.requireNonNull(entityId, "entityId is required");
        Objects.requireNonNull(entityLabel, "entityLabel is required");
        Objects.requireNonNull(action, "action is required");
        Objects.requireNonNull(actorName, "actorName is required");
        Objects.requireNonNull(occurredAt, "occurredAt is required");
        Objects.requireNonNull(descriptionKey, "descriptionKey is required");
        source = source == null || source.isBlank() ? "WEB" : source;
        descriptionArgs = descriptionArgs == null
                ? List.of()
                : Collections.unmodifiableList(
                        new ArrayList<>(descriptionArgs));
        metadata = metadata == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
    }
}
