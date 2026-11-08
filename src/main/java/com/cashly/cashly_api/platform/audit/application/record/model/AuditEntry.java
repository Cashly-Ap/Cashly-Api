package com.cashly.cashly_api.platform.audit.application.record.model;

import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditAction;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditEntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record AuditEntry(
        String module,
        AuditEntityType entityType,
        UUID entityId,
        String entityLabel,
        String secondaryLabel,
        AuditAction action,
        UUID actorId,
        String descriptionKey,
        List<String> descriptionArgs,
        Map<String, Object> metadata) {

    public AuditEntry {
        Objects.requireNonNull(module, "module is required");
        Objects.requireNonNull(entityType, "entityType is required");
        Objects.requireNonNull(entityId, "entityId is required");
        Objects.requireNonNull(entityLabel, "entityLabel is required");
        Objects.requireNonNull(action, "action is required");
        if (descriptionKey == null || descriptionKey.isBlank()) {
            descriptionKey = action.descriptionKey();
        }
        descriptionArgs = descriptionArgs == null
                ? List.of()
                : Collections.unmodifiableList(
                        new ArrayList<>(descriptionArgs));
        metadata = metadata == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
    }

    public static AuditEntry of(String module, AuditEntityType entityType,
            UUID entityId, String entityLabel, String secondaryLabel,
            AuditAction action, UUID actorId, String descriptionKey,
            List<String> descriptionArgs, Map<String, Object> metadata) {
        return new AuditEntry(module, entityType, entityId, entityLabel,
                secondaryLabel, action, actorId, descriptionKey,
                descriptionArgs, metadata);
    }

    public static AuditEntry of(String module, AuditEntityType entityType,
            UUID entityId, String entityLabel, AuditAction action) {
        return new AuditEntry(module, entityType, entityId, entityLabel, null,
                action, null, null, List.of(), Map.of());
    }

    public static AuditEntry of(String module, AuditEntityType entityType,
            UUID entityId, String entityLabel, AuditAction action,
            UUID actorId, Map<String, Object> metadata) {
        return new AuditEntry(module, entityType, entityId, entityLabel, null,
                action, actorId, null, List.of(), metadata);
    }
}
