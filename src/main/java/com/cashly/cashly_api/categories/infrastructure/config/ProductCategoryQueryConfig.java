package com.cashly.cashly_api.categories.infrastructure.config;

import com.cashly.cashly_api.categories.application.api.ProductCategoryQuery;
import com.cashly.cashly_api.categories.application.queries.RepositoryProductCategoryQuery;
import com.cashly.cashly_api.categories.domain.repositories.CategoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductCategoryQueryConfig {
    @Bean
    public ProductCategoryQuery productCategoryQuery(CategoryRepository repository) {
        return new RepositoryProductCategoryQuery(repository);
    }
}
