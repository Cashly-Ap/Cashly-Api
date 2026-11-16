package com.cashly.cashly_api.platform.audit.application.ports.out;

import java.util.Optional;
import java.util.UUID;

public interface AuditActorGateway {

    Optional<ActorSnapshot> find(UUID actorId);

    record ActorSnapshot(UUID id, String name, String photoUrl,
            UUID companyId) {
    }
}
