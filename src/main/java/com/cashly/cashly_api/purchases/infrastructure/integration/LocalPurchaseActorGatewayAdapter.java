package com.cashly.cashly_api.purchases.infrastructure.integration;

import com.cashly.cashly_api.purchases.application.contracts.PurchaseActorGateway;
import com.cashly.cashly_api.auth.user.application.api.ActorIdentityQuery;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class LocalPurchaseActorGatewayAdapter implements PurchaseActorGateway {
    private final ActorIdentityQuery query;
    public LocalPurchaseActorGatewayAdapter(ActorIdentityQuery query) {
        this.query = query;
    }
    @Override
    public Optional<Entry> findById(UUID id) {
        return query.findById(id).map(entry -> new Entry(entry.id(), entry.name()));
    }
}