package com.cashly.cashly_api.payments.application.contracts;

import java.util.Optional;
import java.util.UUID;

public interface PaymentActorGateway {
    Optional<String> findName(UUID actorId);
}
