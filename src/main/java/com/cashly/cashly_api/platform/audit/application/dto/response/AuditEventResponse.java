package com.cashly.cashly_api.platform.audit.application.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AuditEventResponse(
        UUID id,
        String module,
        String entityType,
        UUID entityId,
        String entityLabel,
        String secondaryLabel,
        String action,
        AuditActorResponse actor,
        Instant occurredAt,
        String descriptionKey,
        List<String> descriptionArgs,
        Map<String, Object> metadata) {
}
