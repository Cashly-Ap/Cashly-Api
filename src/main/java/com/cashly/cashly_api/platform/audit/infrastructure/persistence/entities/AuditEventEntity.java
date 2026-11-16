package com.cashly.cashly_api.platform.audit.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_events", indexes = {
        @Index(name = "idx_audit_entity", columnList = "entity_type,entity_id,occurred_at"),
        @Index(name = "idx_audit_actor", columnList = "actor_id,occurred_at"),
        @Index(name = "idx_audit_action", columnList = "module,action,occurred_at"),
        @Index(name = "idx_audit_company_date", columnList = "company_id,occurred_at")})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AuditEventEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "company_id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID companyId;

    @Column(name = "module", length = 60, nullable = false)
    private String module;

    @Column(name = "entity_type", length = 40, nullable = false)
    private String entityType;

    @Column(name = "entity_id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID entityId;

    @Column(name = "entity_label", length = 200, nullable = false)
    private String entityLabel;

    @Column(name = "entity_secondary_label", length = 200)
    private String entitySecondaryLabel;

    @Column(name = "action", length = 40, nullable = false)
    private String action;

    @Column(name = "actor_id", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID actorId;

    @Column(name = "actor_name", length = 200, nullable = false)
    private String actorName;

    @Column(name = "occurred_at", nullable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant occurredAt;

    @Column(name = "description_key", length = 150, nullable = false)
    private String descriptionKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "description_args", columnDefinition = "json")
    private List<String> descriptionArgs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "json")
    private Map<String, Object> metadata;

    @Column(name = "source", length = 20, nullable = false)
    private String source;
}
