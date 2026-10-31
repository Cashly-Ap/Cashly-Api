package com.cashly.cashly_api.catalog.products.infrastructure.persistence.repositories;

import com.cashly.cashly_api.catalog.products.infrastructure.persistence.entities.ProductCodeSequenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProductCodeSequenceRepository
        extends
            JpaRepository<ProductCodeSequenceEntity, Long> {
}
