package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.queries;

import com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.repositories.SpringDataWarehousesProductReferenceRepository;

import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.ProductReferenceQuery;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.UUID;

@Component
public class JpaWarehousesProductReferenceQuery
        implements
            ProductReferenceQuery {
    private final SpringDataWarehousesProductReferenceRepository references;
    public JpaWarehousesProductReferenceQuery(
            SpringDataWarehousesProductReferenceRepository references) {
        this.references = references;
    }
    @Override
    public Set<UUID> findReferencedProductIds(Set<UUID> ids) {
        return ids.isEmpty()
                ? Set.of()
                : Set.copyOf(references.findReferencedProductIds(ids));
    }
}