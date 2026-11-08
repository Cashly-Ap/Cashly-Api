package com.cashly.cashly_api.platform.audit.application.dto.response;

import java.util.UUID;

public record AuditActorOptionResponse(
        UUID id,
        String name,
        String photoUrl,
        long count) {
}
