package com.cashly.cashly_api.returns.infrastructure.adapters.out.persistence.queries;

import com.cashly.cashly_api.returns.infrastructure.adapters.out.persistence.repositories.SpringDataReturnsProductReferenceRepository;

import com.cashly.cashly_api.returns.application.ports.in.ProductReferenceQuery;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.UUID;

@Component
public class JpaReturnsProductReferenceQuery implements ProductReferenceQuery {
    private final SpringDataReturnsProductReferenceRepository references;
    public JpaReturnsProductReferenceQuery(SpringDataReturnsProductReferenceRepository references) {
        this.references = references;
    }
    @Override
    public Set<UUID> findReferencedProductIds(Set<UUID> ids) {
        return ids.isEmpty() ? Set.of() : Set.copyOf(references.findReferencedProductIds(ids));
    }
}