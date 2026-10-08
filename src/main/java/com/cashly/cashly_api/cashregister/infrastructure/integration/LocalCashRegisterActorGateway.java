package com.cashly.cashly_api.cashregister.infrastructure.integration;

import com.cashly.cashly_api.auth.user.application.api.ActorIdentityQuery;
import com.cashly.cashly_api.cashregister.application.contracts.CashRegisterActorGateway;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class LocalCashRegisterActorGateway implements CashRegisterActorGateway {
    private final ActorIdentityQuery actors;

    public LocalCashRegisterActorGateway(ActorIdentityQuery actors) {
        this.actors = actors;
    }

    @Override
    public Optional<Actor> findById(UUID actorId) {
        return actors.findById(actorId).map(actor -> new Actor(actor.id(), actor.name()));
    }
}
