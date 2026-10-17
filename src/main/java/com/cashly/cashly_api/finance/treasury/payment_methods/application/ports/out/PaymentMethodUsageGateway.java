package com.cashly.cashly_api.finance.treasury.payment_methods.application.ports.out;

import java.util.UUID;

public interface PaymentMethodUsageGateway {
    boolean hasAssociatedRecords(UUID paymentMethodId);
}
