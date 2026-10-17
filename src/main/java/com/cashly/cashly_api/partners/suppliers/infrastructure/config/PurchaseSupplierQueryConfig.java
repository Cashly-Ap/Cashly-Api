package com.cashly.cashly_api.partners.suppliers.infrastructure.config;

import com.cashly.cashly_api.partners.suppliers.application.ports.in.PurchaseSupplierQuery;
import com.cashly.cashly_api.partners.suppliers.domain.repository.SupplierRepository;
import com.cashly.cashly_api.partners.suppliers.infrastructure.integration.SupplierPurchaseDirectoryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchaseSupplierQueryConfig {
    @Bean
    public PurchaseSupplierQuery purchaseSupplierQuery(SupplierRepository suppliers) {
        return new SupplierPurchaseDirectoryAdapter(suppliers);
    }
}
