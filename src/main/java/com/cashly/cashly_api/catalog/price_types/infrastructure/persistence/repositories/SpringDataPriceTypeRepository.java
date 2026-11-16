package com.cashly.cashly_api.catalog.price_types.infrastructure.persistence.repositories;

import com.cashly.cashly_api.catalog.price_types.infrastructure.persistence.entities.PriceTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface SpringDataPriceTypeRepository
        extends
            JpaRepository<PriceTypeEntity, UUID> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);

    @Query("""
            SELECT pt FROM PriceTypeEntity pt
            WHERE (:search IS NULL OR LOWER(pt.name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(pt.description) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:active IS NULL OR pt.active = :active)
            """)
    Page<PriceTypeEntity> findAllWithFilters(@Param("search") String search,
            @Param("active") Boolean active, Pageable pageable);

}
