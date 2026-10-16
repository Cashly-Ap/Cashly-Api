package com.cashly.cashly_api.customers.balance.infrastructure.integration;

import com.cashly.cashly_api.auth.user.application.ports.in.ActorIdentityQuery;
import com.cashly.cashly_api.customers.balance.application.ports.out.CustomerActorGateway;
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
