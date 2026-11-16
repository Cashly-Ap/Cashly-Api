package com.cashly.cashly_api.platform.audit.application.get_filter_options.use_case;

import com.cashly.cashly_api.platform.audit.application.dto.response.AuditFilterOptionsResponse;
import com.cashly.cashly_api.platform.audit.application.mappers.AuditMapper;
import com.cashly.cashly_api.platform.audit.application.ports.out.AuditQueryPort;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditEntityType;

import java.util.UUID;

public class GetAuditFilterOptionsUseCase {

    private final AuditQueryPort query;
    private final AuditMapper mapper;

    public GetAuditFilterOptionsUseCase(AuditQueryPort query,
            AuditMapper mapper) {
        this.query = query;
        this.mapper = mapper;
    }

    public AuditFilterOptionsResponse execute(AuditEntityType entityType,
            UUID entityId, String module) {
        return mapper.toResponse(
                query.filterOptions(entityType, entityId, module));
    }
}
