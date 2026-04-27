package com.parsun.parsun_api.categories.infrastructure.persistence.repositories;

import com.parsun.parsun_api.categories.infrastructure.persistence.entities.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query(value = """
        SELECT COUNT(*) FROM products p
        WHERE p.category_id = :categoryId AND p.active = true
        """, nativeQuery = true)
    long countActiveProductsByCategory(@Param("categoryId") UUID categoryId);
}
