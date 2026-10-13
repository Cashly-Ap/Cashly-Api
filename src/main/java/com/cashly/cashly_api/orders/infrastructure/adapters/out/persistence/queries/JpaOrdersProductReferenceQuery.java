package com.cashly.cashly_api.orders.infrastructure.adapters.out.persistence.queries;

import com.cashly.cashly_api.orders.infrastructure.adapters.out.persistence.repositories.SpringDataOrdersProductReferenceRepository;

import com.cashly.cashly_api.orders.application.ports.in.ProductReferenceQuery;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.UUID;

@Component
public class JpaOrdersProductReferenceQuery implements ProductReferenceQuery {
    private final SpringDataOrdersProductReferenceRepository references;
    public JpaOrdersProductReferenceQuery(SpringDataOrdersProductReferenceRepository references) {
        this.references = references;
    }
    @Override
    public Set<UUID> findReferencedProductIds(Set<UUID> ids) {
        return ids.isEmpty() ? Set.of() : Set.copyOf(references.findReferencedProductIds(ids));
    }
}