package com.cashly.cashly_api.platform.audit.infrastructure.integration_tests;

import com.cashly.cashly_api.platform.audit.application.ports.out.AuditQueryPort;
import com.cashly.cashly_api.platform.audit.domain.entity.AuditEvent;
import com.cashly.cashly_api.platform.audit.domain.repository.AuditEventRepository;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditAction;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditEntityType;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditFilter;
import com.cashly.cashly_api.platform.audit.infrastructure.persistence.adapters.JpaAuditRepositoryAdapter;
import com.cashly.cashly_api.platform.audit.infrastructure.persistence.mappers.AuditJpaMapper;
import com.cashly.cashly_api.platform.audit.infrastructure.persistence.queries.JpaAuditQuery;
import com.cashly.cashly_api.platform.company.domain.entity.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({AuditJpaMapper.class, JpaAuditRepositoryAdapter.class,
        JpaAuditQuery.class})
class AuditPersistenceMySqlIT {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AuditEventRepository events;

    @Autowired
    private AuditQueryPort queries;

    private UUID actorOne;
    private UUID actorTwo;

    @BeforeEach
    void prepareActors() {
        jdbc.update("insert ignore into companies(id, legal_name) "
                + "values (UUID_TO_BIN(?), ?)", Company.INSTALLATION_ID.toString(),
                "Audit integration company");
        UUID roleId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        jdbc.update("insert into auth_roles(id, name, active, version) "
                + "values (UUID_TO_BIN(?), ?, true, 0)", roleId.toString(),
                "Audit role " + roleId);
        jdbc.update("insert into auth_departments(id, name, active, version) "
                + "values (UUID_TO_BIN(?), ?, true, 0)", departmentId.toString(),
                "Audit department " + departmentId);
        actorOne = insertUser(roleId, departmentId, "one");
        actorTwo = insertUser(roleId, departmentId, "two");
    }

    @Test
    void shouldPersistEventAndReadItBack() {
        UUID entityId = UUID.randomUUID();
        AuditEvent event = event(entityId, "INV-1", AuditAction.CONFIRM,
                actorOne, "Cashly Administrator",
                Instant.parse("2025-01-01T10:00:00Z"),
                Map.of("statusFrom", "DRAFT", "statusTo", "CONFIRMED"));

        events.save(event);

        AuditEvent found = queries.findById(event.id()).orElseThrow();
        assertThat(found.entityLabel()).isEqualTo("INV-1");
        assertThat(found.actorId()).isEqualTo(actorOne);
        assertThat(found.metadata()).containsEntry("statusFrom", "DRAFT");
    }

    @Test
    void shouldOrderByOccurredAtDescAndPaginate() {
        UUID entityId = UUID.randomUUID();
        events.save(event(entityId, "INV-1", AuditAction.CREATE, actorOne,
                "Admin", Instant.parse("2025-01-01T10:00:00Z"), Map.of()));
        events.save(event(entityId, "INV-1", AuditAction.EDIT, actorOne,
                "Admin", Instant.parse("2025-01-01T11:00:00Z"), Map.of()));
        events.save(event(entityId, "INV-1", AuditAction.CONFIRM, actorOne,
                "Admin", Instant.parse("2025-01-01T12:00:00Z"), Map.of()));

        var firstPage = queries.search(filter(entityId, Set.of(), null, null,
                null, 0, 2));
        var secondPage = queries.search(filter(entityId, Set.of(), null, null,
                null, 1, 2));

        assertThat(firstPage.totalElements()).isEqualTo(3);
        assertThat(firstPage.totalPages()).isEqualTo(2);
        assertThat(firstPage.content()).extracting(AuditEvent::occurredAt)
                .containsExactly(Instant.parse("2025-01-01T12:00:00Z"),
                        Instant.parse("2025-01-01T11:00:00Z"));
        assertThat(secondPage.content()).extracting(AuditEvent::occurredAt)
                .containsExactly(Instant.parse("2025-01-01T10:00:00Z"));
    }

    @Test
    void shouldFilterByActionActorAndDateRange() {
        UUID entityId = UUID.randomUUID();
        events.save(event(entityId, "INV-1", AuditAction.CONFIRM, actorOne,
                "Admin", Instant.parse("2025-01-01T10:00:00Z"), Map.of()));
        events.save(event(entityId, "INV-1", AuditAction.PAY, actorTwo,
                "Operator", Instant.parse("2025-01-02T10:00:00Z"), Map.of()));
        events.save(event(entityId, "INV-1", AuditAction.CREATE, actorOne,
                "Admin", Instant.parse("2025-01-03T10:00:00Z"), Map.of()));

        assertThat(queries.search(filter(entityId,
                Set.of(AuditAction.CONFIRM, AuditAction.PAY), null, null, null,
                0, 20)).content()).hasSize(2);
        assertThat(queries.search(filter(entityId, Set.of(), actorOne, null,
                null, 0, 20)).content()).hasSize(2);
        assertThat(queries.search(filter(entityId, Set.of(), null,
                LocalDate.of(2025, 1, 2), null, 0, 20)).content()).hasSize(2);
        assertThat(queries.search(filter(entityId, Set.of(AuditAction.PAY),
                actorTwo, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 2),
                0, 20)).content()).hasSize(1);
    }

    @Test
    void shouldExposeOnlyValuesPresentForTheRecord() {
        UUID entityId = UUID.randomUUID();
        events.save(event(entityId, "INV-1", AuditAction.CONFIRM, actorOne,
                "Admin", Instant.parse("2025-01-01T10:00:00Z"), Map.of()));
        events.save(event(entityId, "INV-1", AuditAction.PAY, actorTwo,
                "Operator", Instant.parse("2025-01-02T10:00:00Z"), Map.of()));
        events.save(event(UUID.randomUUID(), "INV-2", AuditAction.CREATE,
                actorOne, "Admin", Instant.parse("2025-01-03T10:00:00Z"),
                Map.of()));

        AuditQueryPort.AuditFilterOptions options = queries.filterOptions(
                AuditEntityType.INVOICE, entityId, null);

        assertThat(options.actions()).extracting(
                AuditQueryPort.ActionCount::action)
                .containsExactly(AuditAction.CONFIRM, AuditAction.PAY);
        assertThat(options.actors()).extracting(
                AuditQueryPort.ActorCount::actorId)
                .containsExactly(actorOne, actorTwo);
        assertThat(options.dateRange().min())
                .isEqualTo(Instant.parse("2025-01-01T10:00:00Z"));
        assertThat(options.dateRange().max())
                .isEqualTo(Instant.parse("2025-01-02T10:00:00Z"));
    }

    @Test
    void shouldKeepHistoryWhenReferencedEntityDoesNotExist() {
        UUID missingEntity = UUID.randomUUID();
        AuditEvent event = event(missingEntity, "GONE-1", AuditAction.DELETE,
                actorOne, "Admin", Instant.parse("2025-01-01T10:00:00Z"),
                Map.of());

        events.save(event);

        assertThat(queries.findById(event.id())).isPresent();
        assertThat(queries.search(filter(missingEntity, Set.of(), null, null,
                null, 0, 20)).content()).hasSize(1);
    }

    private AuditFilter filter(UUID entityId, Set<AuditAction> actions,
            UUID actorId, LocalDate from, LocalDate to, int page, int size) {
        return new AuditFilter(AuditEntityType.INVOICE, entityId, null, actions,
                actorId, from, to, page, size);
    }

    private AuditEvent event(UUID entityId, String label, AuditAction action,
            UUID actorId, String actorName, Instant occurredAt,
            Map<String, Object> metadata) {
        return new AuditEvent(UUID.randomUUID(), Company.INSTALLATION_ID,
                "finance", AuditEntityType.INVOICE, entityId, label, null,
                action, actorId, actorName, null, occurredAt,
                action.descriptionKey(), List.of(label), metadata, "WEB");
    }

    private UUID insertUser(UUID roleId, UUID departmentId, String suffix) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into users(id, email, user_name, password_hash, "
                + "role, first_name, last_name, role_id, department_id, active, "
                + "email_verified, created_at, updated_at) values "
                + "(UUID_TO_BIN(?), ?, ?, ?, 'OPERATOR', 'Audit', ?, "
                + "UUID_TO_BIN(?), UUID_TO_BIN(?), true, true, NOW(), NOW())",
                id.toString(), "audit-" + suffix + "-" + id + "@example.test",
                "Audit " + suffix, "hash", "User", roleId.toString(),
                departmentId.toString());
        return id;
    }
}
