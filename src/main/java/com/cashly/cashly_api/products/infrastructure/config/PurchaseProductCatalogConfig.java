package com.cashly.cashly_api.products.infrastructure.config;

import com.cashly.cashly_api.products.application.ports.in.PurchaseProductCatalogQuery;
import com.cashly.cashly_api.products.application.queries.services.RepositoryPurchaseProductCatalogQuery;
import com.cashly.cashly_api.products.domain.repository.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchaseProductCatalogConfig {
    @Bean
    public PurchaseProductCatalogQuery purchaseProductCatalogQuery(ProductRepository products) {
        return new RepositoryPurchaseProductCatalogQuery(products);
    }
}
