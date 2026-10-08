package com.cashly.cashly_api.orders.application.contracts;

import java.util.Optional;
import java.util.UUID;

public interface OrderActorGateway {
    Optional<Actor> findById(UUID actorId);

    record Actor(UUID id, String name) {}
}
