package com.cashly.cashly_api.suppliers.infrastructure.config;

import com.cashly.cashly_api.suppliers.application.ports.in.PurchaseSupplierQuery;
import com.cashly.cashly_api.suppliers.domain.repositories.SupplierRepository;
import com.cashly.cashly_api.suppliers.infrastructure.adapters.out.integration.SupplierPurchaseDirectoryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchaseSupplierQueryConfig {
    @Bean
    public PurchaseSupplierQuery purchaseSupplierQuery(SupplierRepository suppliers) {
        return new SupplierPurchaseDirectoryAdapter(suppliers);
    }
}
