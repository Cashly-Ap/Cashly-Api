package com.cashly.cashly_api.platform.audit.application.ports.in;

import com.cashly.cashly_api.platform.audit.application.dto.response.AuditEventResponse;
import com.cashly.cashly_api.platform.audit.application.dto.response.AuditFilterOptionsResponse;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditEntityType;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditFilter;
import com.cashly.cashly_api.shared.dto.PageResponse;

import java.util.UUID;

public interface AuditQueryService {

    PageResponse<AuditEventResponse> find(AuditFilter filter);

    AuditFilterOptionsResponse filterOptions(AuditEntityType entityType,
            UUID entityId, String module);

    AuditEventResponse get(UUID id);
}
