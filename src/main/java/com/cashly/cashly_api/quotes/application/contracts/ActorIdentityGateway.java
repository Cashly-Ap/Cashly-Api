package com.cashly.cashly_api.quotes.application.contracts;

import java.util.UUID;
import java.util.Optional;

public interface ActorIdentityGateway {
    Optional<Actor> findActor(UUID id);
    Actor requireActor(UUID id);
    record Actor(UUID id, String name) {}
}
