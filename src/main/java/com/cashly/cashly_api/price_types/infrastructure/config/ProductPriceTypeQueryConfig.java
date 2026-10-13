package com.cashly.cashly_api.price_types.infrastructure.config;

import com.cashly.cashly_api.price_types.application.ports.in.ProductPriceTypeQuery;
import com.cashly.cashly_api.price_types.application.queries.RepositoryProductPriceTypeQuery;
import com.cashly.cashly_api.price_types.domain.repositories.PriceTypeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductPriceTypeQueryConfig {
    @Bean
    public ProductPriceTypeQuery productPriceTypeQuery(PriceTypeRepository repository) {
        return new RepositoryProductPriceTypeQuery(repository);
    }
}
