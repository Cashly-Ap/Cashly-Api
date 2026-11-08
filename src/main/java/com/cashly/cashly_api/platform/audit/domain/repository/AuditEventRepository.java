package com.cashly.cashly_api.platform.audit.domain.repository;

import com.cashly.cashly_api.platform.audit.domain.entity.AuditEvent;

public interface AuditEventRepository {
    AuditEvent save(AuditEvent event);
}
