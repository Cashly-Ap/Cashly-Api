package com.cashly.cashly_api.payment_methods.application.contracts;

import java.util.UUID;

public interface PaymentMethodUsageGateway {
    boolean hasAssociatedRecords(UUID paymentMethodId);
}
