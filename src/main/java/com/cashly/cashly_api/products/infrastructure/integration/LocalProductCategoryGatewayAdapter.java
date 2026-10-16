package com.cashly.cashly_api.products.infrastructure.integration;

import com.cashly.cashly_api.products.application.ports.out.ProductCategoryGateway;
import com.cashly.cashly_api.categories.application.ports.in.ProductCategoryQuery;
import com.cashly.cashly_api.categories.application.exceptions.CategoryNotFoundException;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class LocalProductCategoryGatewayAdapter implements ProductCategoryGateway {
    private final ProductCategoryQuery query;

    public LocalProductCategoryGatewayAdapter(ProductCategoryQuery query) {
        this.query = query;
    }

    @Override
    public Optional<Entry> findById(UUID id) {
        return query.findById(id).map(entry -> new Entry(entry.id(), entry.name()));
    }

    @Override
    public Entry require(UUID id) {
        return findById(id).orElseThrow(() -> new CategoryNotFoundException(id));
    }
}
