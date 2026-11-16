package com.cashly.cashly_api.procurement.purchases.infrastructure.config;

import com.cashly.cashly_api.procurement.purchases.application.ports.in.SupplierPurchaseUsageQuery;
import com.cashly.cashly_api.procurement.purchases.application.queries.services.RepositorySupplierPurchaseUsageQuery;
import com.cashly.cashly_api.procurement.purchases.domain.repository.PurchaseRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SupplierPurchaseUsageConfig {
    @Bean
    public SupplierPurchaseUsageQuery supplierPurchaseUsageQuery(
            PurchaseRepository purchases) {
        return new RepositorySupplierPurchaseUsageQuery(purchases);
    }
}
