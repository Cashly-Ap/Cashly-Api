package com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.queries;

import com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.repositories.SpringDataReturnsProductReferenceRepository;

import com.cashly.cashly_api.finance.billing.returns.application.ports.in.ProductReferenceQuery;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.UUID;

@Component
public class JpaReturnsProductReferenceQuery implements ProductReferenceQuery {
    private final SpringDataReturnsProductReferenceRepository references;
    public JpaReturnsProductReferenceQuery(
            SpringDataReturnsProductReferenceRepository references) {
        this.references = references;
    }
    @Override
    public Set<UUID> findReferencedProductIds(Set<UUID> ids) {
        return ids.isEmpty()
                ? Set.of()
                : Set.copyOf(references.findReferencedProductIds(ids));
    }
}