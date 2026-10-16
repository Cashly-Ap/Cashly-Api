package com.cashly.cashly_api.payments.collection.application.ports.out;

import java.util.UUID;

public interface PaymentNotificationGateway {
    void approved(UUID customerId, UUID paymentId);
    void rejected(UUID customerId, UUID paymentId, String reason);
}
