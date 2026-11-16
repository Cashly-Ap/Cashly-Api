package com.cashly.cashly_api.platform.audit.application.dto.response;

import java.util.UUID;

public record AuditActorResponse(UUID id, String name, String photoUrl) {
}
