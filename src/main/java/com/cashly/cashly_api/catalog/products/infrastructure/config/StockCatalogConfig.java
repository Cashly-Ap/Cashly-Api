package com.cashly.cashly_api.catalog.products.infrastructure.config;

import com.cashly.cashly_api.catalog.products.application.ports.in.StockCatalogQuery;
import com.cashly.cashly_api.catalog.products.application.queries.services.RepositoryStockCatalogQuery;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StockCatalogConfig {
    @Bean
    public StockCatalogQuery stockCatalogQuery(ProductRepository products) {
        return new RepositoryStockCatalogQuery(products);
    }
}
