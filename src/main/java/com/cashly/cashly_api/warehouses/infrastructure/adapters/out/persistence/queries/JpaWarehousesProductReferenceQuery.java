package com.cashly.cashly_api.warehouses.infrastructure.adapters.out.persistence.queries;

import com.cashly.cashly_api.warehouses.infrastructure.adapters.out.persistence.repositories.SpringDataWarehousesProductReferenceRepository;

import com.cashly.cashly_api.warehouses.application.ports.in.ProductReferenceQuery;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.UUID;

@Component
public class JpaWarehousesProductReferenceQuery implements ProductReferenceQuery {
    private final SpringDataWarehousesProductReferenceRepository references;
    public JpaWarehousesProductReferenceQuery(SpringDataWarehousesProductReferenceRepository references) {
        this.references = references;
    }
    @Override
    public Set<UUID> findReferencedProductIds(Set<UUID> ids) {
        return ids.isEmpty() ? Set.of() : Set.copyOf(references.findReferencedProductIds(ids));
    }
}