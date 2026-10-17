package com.cashly.cashly_api.sales.quotes.infrastructure.integration;

import com.cashly.cashly_api.platform.auth.user.application.ports.in.ActorIdentityQuery;
import com.cashly.cashly_api.sales.quotes.application.ports.out.ActorIdentityGateway;
import org.springframework.stereotype.Component;
import java.util.UUID;
import java.util.Optional;

@Component
public class QuoteActorIdentityAdapter implements ActorIdentityGateway {
    private final ActorIdentityQuery query;

    public QuoteActorIdentityAdapter(ActorIdentityQuery query) {
        this.query = query;
    }

    @Override
    public Actor requireActor(UUID id) {
        return findActor(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    @Override
    public Optional<Actor> findActor(UUID id) {
        return query.findById(id).map(actor -> new Actor(actor.id(), actor.name()));
    }
}
