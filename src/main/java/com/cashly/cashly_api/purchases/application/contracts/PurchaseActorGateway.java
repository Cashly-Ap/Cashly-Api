package com.cashly.cashly_api.purchases.application.contracts;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseActorGateway {
    Optional<Entry> findById(UUID id);
    record Entry(UUID id, String name) {}
}