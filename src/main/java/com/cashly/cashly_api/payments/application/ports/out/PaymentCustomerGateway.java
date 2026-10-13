package com.cashly.cashly_api.payments.application.ports.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentCustomerGateway {
    boolean exists(UUID customerId);
    void consumeInvoiceBalance(BalanceConsumption consumption);

    record BalanceConsumption(UUID customerId, BigDecimal amount, UUID invoiceId,
                              String reference, UUID actorId) {}
}
