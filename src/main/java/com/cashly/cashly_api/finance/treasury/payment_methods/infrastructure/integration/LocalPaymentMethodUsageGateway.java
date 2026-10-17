package com.cashly.cashly_api.finance.treasury.payment_methods.infrastructure.integration;

import com.cashly.cashly_api.finance.treasury.payment_methods.application.ports.out.PaymentMethodUsageGateway;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class LocalPaymentMethodUsageGateway implements PaymentMethodUsageGateway {
    private final com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.in.QueryPaymentMethodUsage payments;
    private final com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.in.QueryPaymentMethodUsage cashregister;

    public LocalPaymentMethodUsageGateway(
            com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.in.QueryPaymentMethodUsage payments,
            com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.in.QueryPaymentMethodUsage cashregister) {
        this.payments = payments;
        this.cashregister = cashregister;
    }

    @Override
    public boolean hasAssociatedRecords(UUID paymentMethodId) {
        return payments.hasUsage(paymentMethodId) || cashregister.hasUsage(paymentMethodId);
    }
}
