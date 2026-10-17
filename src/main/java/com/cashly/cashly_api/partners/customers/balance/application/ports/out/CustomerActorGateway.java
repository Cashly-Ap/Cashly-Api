package com.cashly.cashly_api.partners.customers.balance.application.ports.out;

import java.util.Optional;
import java.util.UUID;

public interface CustomerActorGateway {
    Optional<CustomerActor> findById(UUID actorId);

    record CustomerActor(UUID id, String name) {}
}
