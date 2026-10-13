package com.cashly.cashly_api.payment_methods.application.ports.out;

import java.util.UUID;

public interface PaymentMethodUsageGateway {
    boolean hasAssociatedRecords(UUID paymentMethodId);
}
