package com.cashly.cashly_api.cashregister.infrastructure.query;

import com.cashly.cashly_api.cashregister.application.api.QueryPaymentMethodUsage;
import com.cashly.cashly_api.cashregister.infrastructure.persistence.CashRegisterJpaRepository;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component("cashregisterPaymentMethodUsageQuery")
public class JpaPaymentMethodUsageQuery implements QueryPaymentMethodUsage {
    private final CashRegisterJpaRepository repository;

    public JpaPaymentMethodUsageQuery(CashRegisterJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean hasUsage(UUID paymentMethodId) {
        return repository.countDetailsByPaymentMethodId(paymentMethodId) > 0;
    }
}
