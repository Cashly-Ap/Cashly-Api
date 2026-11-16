package com.cashly.cashly_api.platform.audit.infrastructure.web;

import com.cashly.cashly_api.platform.audit.application.dto.response.AuditEventResponse;
import com.cashly.cashly_api.platform.audit.application.dto.response.AuditFilterOptionsResponse;
import com.cashly.cashly_api.platform.audit.application.exceptions.InvalidAuditFilterException;
import com.cashly.cashly_api.platform.audit.application.exceptions.UnknownAuditEntityTypeException;
import com.cashly.cashly_api.platform.audit.application.ports.in.AuditQueryService;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditAction;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditEntityType;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditFilter;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit-events")
public class AuditController {

    private final AuditQueryService queries;
    private final int maxPageSize;

    public AuditController(AuditQueryService queries,
            @Value("${cashly.audit.max-page-size:100}") int maxPageSize) {
        this.queries = queries;
        this.maxPageSize = maxPageSize;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_TRAIL_VIEW_LIST')")
    public ResponseEntity<PageResponse<AuditEventResponse>> find(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) List<String> action,
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (size > maxPageSize) {
            throw new InvalidAuditFilterException(
                    "audit.errors.invalidPageSize", maxPageSize);
        }
        AuditFilter filter = new AuditFilter(parseEntityType(entityType),
                entityId, module, parseActions(action), actorId, from, to, page,
                size);
        return ResponseEntity.ok(queries.find(filter));
    }

    @GetMapping("/filter-options")
    @PreAuthorize("hasAuthority('AUDIT_TRAIL_VIEW_LIST')")
    public ResponseEntity<AuditFilterOptionsResponse> filterOptions(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) String module) {
        return ResponseEntity.ok(queries.filterOptions(
                parseEntityType(entityType), entityId, module));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('AUDIT_TRAIL_VIEW_LIST')")
    public ResponseEntity<AuditEventResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(queries.get(id));
    }

    private AuditEntityType parseEntityType(String value) {
        try {
            return AuditEntityType.parse(value);
        } catch (IllegalArgumentException unknown) {
            throw new UnknownAuditEntityTypeException(value);
        }
    }

    private Set<AuditAction> parseActions(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        return values.stream().map(this::parseAction)
                .collect(Collectors.toUnmodifiableSet());
    }

    private AuditAction parseAction(String value) {
        try {
            return AuditAction.valueOf(value.trim().toUpperCase(
                    java.util.Locale.ROOT));
        } catch (IllegalArgumentException unknown) {
            throw new InvalidAuditFilterException(
                    "audit.errors.unknownAction", value);
        }
    }
}
