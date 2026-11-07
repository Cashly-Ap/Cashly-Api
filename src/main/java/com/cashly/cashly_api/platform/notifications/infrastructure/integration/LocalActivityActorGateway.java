package com.cashly.cashly_api.platform.notifications.infrastructure.integration;

import com.cashly.cashly_api.platform.auth.user.application.ports.in.ActorIdentityQuery;
import com.cashly.cashly_api.platform.notifications.application.ports.out.ActivityActorGateway;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class LocalActivityActorGateway implements ActivityActorGateway {

    private final ActorIdentityQuery actors;

    public LocalActivityActorGateway(ActorIdentityQuery actors) {
        this.actors = actors;
    }

    @Override
    public Optional<String> actorName(UUID actorId) {
        return actors.findById(actorId).map(ActorIdentityQuery.Actor::name);
    }
}
