package com.cashly.cashly_api.payments.infrastructure.query;

import com.cashly.cashly_api.payments.application.api.QueryPaymentMethodUsage;
import com.cashly.cashly_api.payments.infrastructure.persistence.repositories.PaymentJpaRepository;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component("paymentsPaymentMethodUsageQuery")
public class JpaPaymentMethodUsageQuery implements QueryPaymentMethodUsage {
    private final PaymentJpaRepository repository;

    public JpaPaymentMethodUsageQuery(PaymentJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean hasUsage(UUID paymentMethodId) {
        return repository.countByPaymentMethodId(paymentMethodId) > 0;
    }
}
