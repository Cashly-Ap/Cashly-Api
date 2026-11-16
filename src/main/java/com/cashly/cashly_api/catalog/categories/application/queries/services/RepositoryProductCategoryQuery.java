package com.cashly.cashly_api.catalog.categories.application.queries.services;

import com.cashly.cashly_api.catalog.categories.application.ports.in.ProductCategoryQuery;
import com.cashly.cashly_api.catalog.categories.domain.repository.CategoryRepository;
import java.util.Optional;
import java.util.UUID;

public class RepositoryProductCategoryQuery implements ProductCategoryQuery {
    private final CategoryRepository repository;

    public RepositoryProductCategoryQuery(CategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Entry> findById(UUID id) {
        return repository.findById(id)
                .map(entity -> new Entry(entity.getId(), entity.getName()));
    }
}
