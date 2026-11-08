package com.cashly.cashly_api.platform.audit.domain.valueobjects;

import com.cashly.cashly_api.platform.audit.domain.exceptions.AuditFilterException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditFilterTest {

    @Test
    void shouldRejectInvertedDateRange() {
        assertThatThrownBy(() -> new AuditFilter(AuditEntityType.INVOICE,
                UUID.randomUUID(), null, Set.of(), null,
                LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1), 0, 20))
                .isInstanceOf(AuditFilterException.class)
                .hasMessage("audit.errors.invalidRange");
    }

    @Test
    void shouldRequireEntityOrModule() {
        assertThatThrownBy(() -> new AuditFilter(null, null, null, Set.of(),
                null, null, null, 0, 20))
                .isInstanceOf(AuditFilterException.class)
                .hasMessage("audit.errors.entityRequired");
    }

    @Test
    void shouldRequireEntityIdWhenEntityTypeIsPresent() {
        assertThatThrownBy(() -> new AuditFilter(AuditEntityType.INVOICE, null,
                null, Set.of(), null, null, null, 0, 20))
                .isInstanceOf(AuditFilterException.class)
                .hasMessage("audit.errors.entityRequired");
    }

    @Test
    void shouldRejectInvalidPageSize() {
        assertThatThrownBy(() -> new AuditFilter(AuditEntityType.INVOICE,
                UUID.randomUUID(), null, Set.of(), null, null, null, 0, 0))
                .isInstanceOf(AuditFilterException.class)
                .hasMessage("audit.errors.invalidPageSize");
    }

    @Test
    void shouldBuildInclusiveDateBounds() {
        AuditFilter filter = new AuditFilter(AuditEntityType.INVOICE,
                UUID.randomUUID(), null, Set.of(AuditAction.CONFIRM), null,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 2), 0, 20);

        assertThat(filter.fromInstant()).isEqualTo("2025-01-01T00:00:00Z");
        assertThat(filter.toExclusiveInstant()).isEqualTo("2025-01-03T00:00:00Z");
        assertThat(filter.actions()).containsExactly(AuditAction.CONFIRM);
    }
}
