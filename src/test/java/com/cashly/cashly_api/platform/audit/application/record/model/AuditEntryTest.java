package com.cashly.cashly_api.platform.audit.application.record.model;

import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditAction;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditEntityType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditEntryTest {

    @Test
    void shouldDeriveDescriptionKeyFromAction() {
        AuditEntry entry = AuditEntry.of("finance", AuditEntityType.INVOICE,
                UUID.randomUUID(), "INV-1", AuditAction.CONFIRM);

        assertThat(entry.descriptionKey())
                .isEqualTo("audit.events.confirm.description");
        assertThat(entry.descriptionArgs()).isEmpty();
        assertThat(entry.metadata()).isEmpty();
    }

    @Test
    void shouldKeepExplicitDescriptionAndMetadata() {
        UUID id = UUID.randomUUID();
        AuditEntry entry = AuditEntry.of("finance", AuditEntityType.INVOICE, id,
                "INV-1", null, AuditAction.PAY, null, "audit.events.pay.custom",
                List.of("13.00"), Map.of("method", "CASH"));

        assertThat(entry.descriptionKey()).isEqualTo("audit.events.pay.custom");
        assertThat(entry.descriptionArgs()).containsExactly("13.00");
        assertThat(entry.metadata()).containsEntry("method", "CASH");
    }

    @Test
    void shouldRejectMissingRequiredFields() {
        assertThatThrownBy(() -> new AuditEntry(null, AuditEntityType.INVOICE,
                UUID.randomUUID(), "INV-1", null, AuditAction.CREATE, null,
                null, List.of(), Map.of()))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new AuditEntry("finance", null,
                UUID.randomUUID(), "INV-1", null, AuditAction.CREATE, null,
                null, List.of(), Map.of()))
                .isInstanceOf(NullPointerException.class);
    }
}
