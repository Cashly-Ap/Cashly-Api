package com.cashly.cashly_api.platform.audit.application.record.use_case;

import com.cashly.cashly_api.platform.audit.application.ports.out.AuditActorGateway;
import com.cashly.cashly_api.platform.audit.application.record.model.AuditEntry;
import com.cashly.cashly_api.platform.audit.domain.entity.AuditEvent;
import com.cashly.cashly_api.platform.audit.domain.repository.AuditEventRepository;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditAction;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditEntityType;
import com.cashly.cashly_api.platform.auth.authorization.application.ports.in.CurrentActor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecordAuditUseCaseTest {

    private final UUID companyId = UUID.randomUUID();
    private final UUID actorId = UUID.randomUUID();
    private final Instant now = Instant.parse("2025-01-01T10:00:00Z");
    private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);

    @Test
    void shouldRecordEventWithResolvedActorSnapshot() {
        AuditEventRepository events = mock(AuditEventRepository.class);
        AuditActorGateway actors = mock(AuditActorGateway.class);
        CurrentActor currentActor = mock(CurrentActor.class);
        when(actors.find(actorId)).thenReturn(Optional.of(
                new AuditActorGateway.ActorSnapshot(actorId, "Juan",
                        "/photo.png", companyId)));
        RecordAuditUseCase useCase = new RecordAuditUseCase(events, actors,
                currentActor, clock, companyId, true);

        useCase.record(AuditEntry.of("finance", AuditEntityType.INVOICE,
                UUID.randomUUID(), "INV-1", AuditAction.CONFIRM, actorId,
                Map.of("status", "CONFIRMED")));

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor
                .forClass(AuditEvent.class);
        verify(events).save(captor.capture());
        AuditEvent saved = captor.getValue();
        assertThat(saved.actorId()).isEqualTo(actorId);
        assertThat(saved.actorName()).isEqualTo("Juan");
        assertThat(saved.actorPhotoUrl()).isEqualTo("/photo.png");
        assertThat(saved.companyId()).isEqualTo(companyId);
        assertThat(saved.occurredAt()).isEqualTo(now);
        assertThat(saved.source()).isEqualTo("WEB");
        assertThat(saved.metadata()).containsEntry("status", "CONFIRMED");
    }

    @Test
    void shouldFallbackToSystemActorWhenAuthenticationIsUnavailable() {
        AuditEventRepository events = mock(AuditEventRepository.class);
        AuditActorGateway actors = mock(AuditActorGateway.class);
        CurrentActor currentActor = mock(CurrentActor.class);
        when(currentActor.requireId())
                .thenThrow(new AccessDeniedException("Authentication required"));
        RecordAuditUseCase useCase = new RecordAuditUseCase(events, actors,
                currentActor, clock, companyId, true);

        useCase.record(AuditEntry.of("finance", AuditEntityType.INVOICE,
                UUID.randomUUID(), "INV-1", AuditAction.CREATE));

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor
                .forClass(AuditEvent.class);
        verify(events).save(captor.capture());
        assertThat(captor.getValue().actorId()).isNull();
        assertThat(captor.getValue().actorName()).isEqualTo("Sistema");
        assertThat(captor.getValue().companyId()).isEqualTo(companyId);
        verify(actors, never()).find(any());
    }

    @Test
    void shouldFailClosedWhenRepositoryFails() {
        AuditEventRepository events = mock(AuditEventRepository.class);
        AuditActorGateway actors = mock(AuditActorGateway.class);
        CurrentActor currentActor = mock(CurrentActor.class);
        when(events.save(any())).thenThrow(new IllegalStateException("boom"));
        RecordAuditUseCase useCase = new RecordAuditUseCase(events, actors,
                currentActor, clock, companyId, true);

        assertThatThrownBy(() -> useCase.record(AuditEntry.of("finance",
                AuditEntityType.INVOICE, UUID.randomUUID(), "INV-1",
                AuditAction.CREATE)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
    }

    @Test
    void shouldContinueWhenFailClosedIsDisabled() {
        AuditEventRepository events = mock(AuditEventRepository.class);
        AuditActorGateway actors = mock(AuditActorGateway.class);
        CurrentActor currentActor = mock(CurrentActor.class);
        when(events.save(any())).thenThrow(new IllegalStateException("boom"));
        RecordAuditUseCase useCase = new RecordAuditUseCase(events, actors,
                currentActor, clock, companyId, false);

        assertThatCode(() -> useCase.record(AuditEntry.of("finance",
                AuditEntityType.INVOICE, UUID.randomUUID(), "INV-1",
                AuditAction.CREATE))).doesNotThrowAnyException();
    }
}
