package com.cashly.cashly_api.products.infrastructure.config;

import com.cashly.cashly_api.products.application.ports.in.StockCatalogQuery;
import com.cashly.cashly_api.products.application.queries.RepositoryStockCatalogQuery;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StockCatalogConfig {
    @Bean
    public StockCatalogQuery stockCatalogQuery(ProductRepository products) {
        return new RepositoryStockCatalogQuery(products);
    }
}
