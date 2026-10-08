package com.cashly.cashly_api.payment_methods.infrastructure.integration;

import com.cashly.cashly_api.payment_methods.application.contracts.PaymentMethodUsageGateway;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class LocalPaymentMethodUsageGateway implements PaymentMethodUsageGateway {
    private final com.cashly.cashly_api.payments.application.api.QueryPaymentMethodUsage payments;
    private final com.cashly.cashly_api.cashregister.application.api.QueryPaymentMethodUsage cashregister;

    public LocalPaymentMethodUsageGateway(
            com.cashly.cashly_api.payments.application.api.QueryPaymentMethodUsage payments,
            com.cashly.cashly_api.cashregister.application.api.QueryPaymentMethodUsage cashregister) {
        this.payments = payments;
        this.cashregister = cashregister;
    }

    @Override
    public boolean hasAssociatedRecords(UUID paymentMethodId) {
        return payments.hasUsage(paymentMethodId) || cashregister.hasUsage(paymentMethodId);
    }
}
