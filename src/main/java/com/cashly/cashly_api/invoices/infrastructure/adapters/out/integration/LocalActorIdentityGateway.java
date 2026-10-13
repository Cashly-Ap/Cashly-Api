package com.cashly.cashly_api.invoices.infrastructure.adapters.out.integration;
import com.cashly.cashly_api.invoices.application.ports.out.ActorIdentityGateway;
import com.cashly.cashly_api.auth.user.application.ports.in.ActorIdentityQuery;
import org.springframework.stereotype.Component;
import java.util.UUID;
import java.util.Optional;
@Component
public class LocalActorIdentityGateway implements ActorIdentityGateway {
    private final ActorIdentityQuery query;
    public LocalActorIdentityGateway(ActorIdentityQuery query) { this.query = query; }
    public Actor requireActor(UUID id) {
        return findActor(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }
    public Optional<Actor> findActor(UUID id) {
        return query.findById(id).map(actor -> new Actor(actor.id(), actor.name()));
    }
    public boolean matchesPassword(UUID id, String password) { return query.matchesPassword(id, password); }
}
