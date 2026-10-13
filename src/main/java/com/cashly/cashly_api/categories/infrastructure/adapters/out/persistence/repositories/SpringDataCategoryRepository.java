package com.cashly.cashly_api.categories.infrastructure.adapters.out.persistence.repositories;

import com.cashly.cashly_api.categories.infrastructure.adapters.out.persistence.entities.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface SpringDataCategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);

    @Query("""
        SELECT c FROM CategoryEntity c
        WHERE (:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:active IS NULL OR c.active = :active)
        """)
    Page<CategoryEntity> findAllWithFilters(
            @Param("search") String search,
            @Param("active") Boolean active,
            Pageable pageable);

}
