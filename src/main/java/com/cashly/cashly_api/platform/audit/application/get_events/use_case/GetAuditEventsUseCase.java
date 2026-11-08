package com.cashly.cashly_api.platform.audit.application.get_events.use_case;

import com.cashly.cashly_api.platform.audit.application.dto.response.AuditEventResponse;
import com.cashly.cashly_api.platform.audit.application.dto.response.AuditFilterOptionsResponse;
import com.cashly.cashly_api.platform.audit.application.exceptions.AuditEventNotFoundException;
import com.cashly.cashly_api.platform.audit.application.get_filter_options.use_case.GetAuditFilterOptionsUseCase;
import com.cashly.cashly_api.platform.audit.application.mappers.AuditMapper;
import com.cashly.cashly_api.platform.audit.application.ports.in.AuditQueryService;
import com.cashly.cashly_api.platform.audit.application.ports.out.AuditQueryPort;
import com.cashly.cashly_api.platform.audit.domain.entity.AuditEvent;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditEntityType;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditFilter;
import com.cashly.cashly_api.shared.dto.PageResponse;

import java.util.UUID;

public class GetAuditEventsUseCase implements AuditQueryService {

    private final AuditQueryPort query;
    private final AuditMapper mapper;
    private final GetAuditFilterOptionsUseCase filterOptions;

    public GetAuditEventsUseCase(AuditQueryPort query, AuditMapper mapper,
            GetAuditFilterOptionsUseCase filterOptions) {
        this.query = query;
        this.mapper = mapper;
        this.filterOptions = filterOptions;
    }

    @Override
    public PageResponse<AuditEventResponse> find(AuditFilter filter) {
        PageResponse<AuditEvent> page = query.search(filter);
        return new PageResponse<>(
                page.content().stream().map(mapper::toResponse).toList(),
                page.totalElements(), page.totalPages(), page.size(),
                page.number(), page.first(), page.last(), page.empty());
    }

    @Override
    public AuditFilterOptionsResponse filterOptions(AuditEntityType entityType,
            UUID entityId, String module) {
        return filterOptions.execute(entityType, entityId, module);
    }

    @Override
    public AuditEventResponse get(UUID id) {
        return query.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new AuditEventNotFoundException(
                        "audit.errors.eventNotFound"));
    }
}
