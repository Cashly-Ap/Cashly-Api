package com.cashly.cashly_api.categories.infrastructure.config;

import com.cashly.cashly_api.categories.application.ports.in.ProductCategoryQuery;
import com.cashly.cashly_api.categories.application.queries.services.RepositoryProductCategoryQuery;
import com.cashly.cashly_api.categories.domain.repository.CategoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductCategoryQueryConfig {
    @Bean
    public ProductCategoryQuery productCategoryQuery(CategoryRepository repository) {
        return new RepositoryProductCategoryQuery(repository);
    }
}
