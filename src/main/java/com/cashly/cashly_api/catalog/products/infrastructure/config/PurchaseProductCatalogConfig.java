package com.cashly.cashly_api.catalog.products.infrastructure.config;

import com.cashly.cashly_api.catalog.products.application.ports.in.PurchaseProductCatalogQuery;
import com.cashly.cashly_api.catalog.products.application.queries.services.RepositoryPurchaseProductCatalogQuery;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchaseProductCatalogConfig {
    @Bean
    public PurchaseProductCatalogQuery purchaseProductCatalogQuery(ProductRepository products) {
        return new RepositoryPurchaseProductCatalogQuery(products);
    }
}
