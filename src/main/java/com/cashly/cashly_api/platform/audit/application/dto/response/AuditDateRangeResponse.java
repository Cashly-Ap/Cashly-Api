package com.cashly.cashly_api.platform.audit.application.dto.response;

import java.time.Instant;

public record AuditDateRangeResponse(Instant min, Instant max) {
}
