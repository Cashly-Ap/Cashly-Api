package com.cashly.cashly_api.invoices.application.contracts;
import java.util.UUID;
import java.util.Optional;
public interface ActorIdentityGateway {
    Actor requireActor(UUID id);
    Optional<Actor> findActor(UUID id);
    boolean matchesPassword(UUID id, String password);
    record Actor(UUID id, String name) {}
}
