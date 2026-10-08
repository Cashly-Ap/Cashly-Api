package com.cashly.cashly_api.customers.infrastructure.integration;

import com.cashly.cashly_api.auth.user.application.api.ActorIdentityQuery;
import com.cashly.cashly_api.customers.application.contracts.CustomerActorGateway;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LocalCustomerActorGateway implements CustomerActorGateway {
    private final ActorIdentityQuery actors;

    public LocalCustomerActorGateway(ActorIdentityQuery actors) {
        this.actors = actors;
    }

    @Override
    public java.util.Optional<CustomerActor> findById(UUID actorId) {
        return actors.findById(actorId).map(actor -> new CustomerActor(actor.id(), actor.name()));
    }
}
