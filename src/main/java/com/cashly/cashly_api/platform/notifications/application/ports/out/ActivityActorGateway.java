package com.cashly.cashly_api.platform.notifications.application.ports.out;

import java.util.Optional;
import java.util.UUID;

public interface ActivityActorGateway {
    Optional<String> actorName(UUID actorId);
}
