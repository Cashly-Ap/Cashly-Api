package com.cashly.cashly_api.catalog.products.infrastructure.persistence.repositories;

import com.cashly.cashly_api.catalog.products.infrastructure.persistence.entities.ProductEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface SpringDataProductUsageRepository extends Repository<ProductEntity, UUID> {
    @Query(value = """
        SELECT COUNT(*) FROM products p
        WHERE p.category_id = :categoryId AND p.active = true
        """, nativeQuery = true)
    long countActiveProductsByCategory(@Param("categoryId") UUID categoryId);

    @Query("""
        SELECT p.categoryId, COUNT(p) FROM ProductEntity p
        WHERE p.categoryId IN :ids AND p.active = true
        GROUP BY p.categoryId
        """)
    List<Object[]> countActiveProductsByCategoryIds(@Param("ids") Set<UUID> ids);

    @Query("""
        SELECT COUNT(pp) > 0 FROM ProductPriceEntity pp
        WHERE pp.priceTypeId = :priceTypeId
        """)
    boolean isInUseByProducts(@Param("priceTypeId") UUID priceTypeId);

    @Query("""
        SELECT DISTINCT pp.priceTypeId FROM ProductPriceEntity pp
        WHERE pp.priceTypeId IN :priceTypeIds
        """)
    Set<UUID> findIdsInUseByProducts(@Param("priceTypeIds") Set<UUID> priceTypeIds);
}
