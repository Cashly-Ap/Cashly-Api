package com.cashly.cashly_api.orders.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.auth.user.application.ports.in.ActorIdentityQuery;
import com.cashly.cashly_api.orders.application.ports.out.OrderActorGateway;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class LocalOrderActorAdapter implements OrderActorGateway {
    private final ActorIdentityQuery actors;

    public LocalOrderActorAdapter(ActorIdentityQuery actors) {
        this.actors = actors;
    }

    @Override
    public Optional<Actor> findById(UUID actorId) {
        return actors.findById(actorId).map(actor -> new Actor(actor.id(), actor.name()));
    }
}
