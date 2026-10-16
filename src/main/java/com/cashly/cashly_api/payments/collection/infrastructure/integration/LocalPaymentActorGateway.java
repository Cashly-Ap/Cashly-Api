package com.cashly.cashly_api.payments.collection.infrastructure.integration;

import com.cashly.cashly_api.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.payments.collection.application.ports.out.PaymentActorGateway;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class LocalPaymentActorGateway implements PaymentActorGateway {
    private final UserRepository users;

    public LocalPaymentActorGateway(UserRepository users) {
        this.users = users;
    }

    @Override
    public Optional<String> findName(UUID actorId) {
        return users.findById(new UserId(actorId)).map(user -> user.getUserName().getValue());
    }
}
