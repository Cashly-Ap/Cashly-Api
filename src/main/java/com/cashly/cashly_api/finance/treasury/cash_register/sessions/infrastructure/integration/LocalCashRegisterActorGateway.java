package com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.integration;

import com.cashly.cashly_api.platform.auth.user.application.ports.in.ActorIdentityQuery;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.out.CashRegisterActorGateway;
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
