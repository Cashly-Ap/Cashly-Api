package com.cashly.cashly_api.products.infrastructure.adapters.out.persistence.repositories;

import com.cashly.cashly_api.products.infrastructure.adapters.out.persistence.entities.ProductCodeSequenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProductCodeSequenceRepository extends JpaRepository<ProductCodeSequenceEntity, Long> {
}
