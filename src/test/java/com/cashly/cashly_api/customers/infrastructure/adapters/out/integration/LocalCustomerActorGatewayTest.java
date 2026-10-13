package com.cashly.cashly_api.customers.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.auth.user.application.ports.in.ActorIdentityQuery;
import com.cashly.cashly_api.customers.application.ports.out.CustomerActorGateway.CustomerActor;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocalCustomerActorGatewayTest {
    @Test
    void maps_actor_identity_to_customer_owned_contract() {
        ActorIdentityQuery actors = mock(ActorIdentityQuery.class);
        UUID id = UUID.randomUUID();
        when(actors.findById(id)).thenReturn(Optional.of(new ActorIdentityQuery.Actor(id, "Operator")));

        var actor = new LocalCustomerActorGateway(actors).findById(id);

        assertThat(actor).contains(new CustomerActor(id, "Operator"));
    }
}
