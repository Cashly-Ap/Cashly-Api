package com.cashly.cashly_api.platform.audit.application.dto.response;

import java.util.List;

public record AuditFilterOptionsResponse(
        List<AuditActionOptionResponse> actions,
        List<AuditActorOptionResponse> actors,
        AuditDateRangeResponse dateRange) {
}
