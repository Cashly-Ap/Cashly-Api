package com.cashly.cashly_api.platform.audit.application.dto.response;

public record AuditActionOptionResponse(
        String value,
        String labelKey,
        long count) {
}
