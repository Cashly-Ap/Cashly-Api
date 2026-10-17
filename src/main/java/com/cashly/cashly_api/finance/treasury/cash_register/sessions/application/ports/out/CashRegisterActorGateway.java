package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.out;

import java.util.Optional;
import java.util.UUID;

public interface CashRegisterActorGateway {
    Optional<Actor> findById(UUID actorId);

    record Actor(UUID id, String name) {}
}
