package com.cashly.cashly_api.platform.audit.application.record.use_case;

import com.cashly.cashly_api.platform.audit.application.ports.in.AuditRecorder;
import com.cashly.cashly_api.platform.audit.application.ports.out.AuditActorGateway;
import com.cashly.cashly_api.platform.audit.application.record.model.AuditEntry;
import com.cashly.cashly_api.platform.audit.domain.entity.AuditEvent;
import com.cashly.cashly_api.platform.audit.domain.repository.AuditEventRepository;
import com.cashly.cashly_api.platform.auth.authorization.application.ports.in.CurrentActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

public class RecordAuditUseCase implements AuditRecorder {

    private static final Logger log = LoggerFactory
            .getLogger(RecordAuditUseCase.class);
    private static final String SYSTEM_ACTOR = "Sistema";

    private final AuditEventRepository events;
    private final AuditActorGateway actors;
    private final CurrentActor currentActor;
    private final Clock clock;
    private final UUID fallbackCompanyId;
    private final boolean failClosed;

    public RecordAuditUseCase(AuditEventRepository events,
            AuditActorGateway actors, CurrentActor currentActor, Clock clock,
            UUID fallbackCompanyId, boolean failClosed) {
        this.events = events;
        this.actors = actors;
        this.currentActor = currentActor;
        this.clock = clock;
        this.fallbackCompanyId = fallbackCompanyId;
        this.failClosed = failClosed;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void record(AuditEntry entry) {
        if (entry == null) {
            return;
        }
        try {
            execute(entry);
        } catch (RuntimeException exception) {
            if (failClosed) {
                throw exception;
            }
            log.warn("Audit event could not be recorded for {}.{}: {}",
                    entry.module(), entry.action(), exception.getMessage());
        }
    }

    private void execute(AuditEntry entry) {
        UUID actorId = resolveActorId(entry.actorId());
        Optional<AuditActorGateway.ActorSnapshot> actor = actorId == null
                ? Optional.empty()
                : actors.find(actorId);
        UUID companyId = actor
                .map(AuditActorGateway.ActorSnapshot::companyId)
                .orElse(fallbackCompanyId);
        String actorName = actor.map(AuditActorGateway.ActorSnapshot::name)
                .orElse(SYSTEM_ACTOR);
        String actorPhotoUrl = actor
                .map(AuditActorGateway.ActorSnapshot::photoUrl).orElse(null);
        AuditEvent event = new AuditEvent(UUID.randomUUID(), companyId,
                entry.module(), entry.entityType(), entry.entityId(),
                entry.entityLabel(), entry.secondaryLabel(), entry.action(),
                actorId, actorName, actorPhotoUrl, clock.instant(),
                entry.descriptionKey(), entry.descriptionArgs(),
                entry.metadata(), "WEB");
        events.save(event);
    }

    private UUID resolveActorId(UUID explicit) {
        if (explicit != null) {
            return explicit;
        }
        try {
            return currentActor.requireId();
        } catch (RuntimeException unavailable) {
            return null;
        }
    }
}
