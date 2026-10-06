package com.cashly.cashly_api.products.infrastructure.persistence.repositories;

import com.cashly.cashly_api.products.infrastructure.persistence.entities.ProductEntity;
import com.cashly.cashly_api.warehouses.infrastructure.persistence.entities.WarehouseStockEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SpringDataProductRepository extends JpaRepository<ProductEntity, UUID> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);

    boolean existsByCategoryId(UUID categoryId);

    @Query("""
        SELECT p FROM ProductEntity p
        WHERE (:search IS NULL
               OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:categoryId IS NULL OR p.categoryId = :categoryId)
        AND (:active IS NULL OR p.active = :active)
        AND (:composite IS NULL OR p.composite = :composite)
        """)
    Page<ProductEntity> findAllWithFilters(
            @Param("search") String search,
            @Param("categoryId") UUID categoryId,
            @Param("active") Boolean active,
            @Param("composite") Boolean composite,
            Pageable pageable);

    @Query("""
        SELECT DISTINCT p FROM ProductEntity p
        LEFT JOIN FETCH p.components
        WHERE p.id = :id
        """)
    Optional<ProductEntity> findWithComponentsById(@Param("id") UUID id);

    @Modifying
    @Query("DELETE FROM ProductComponentEntity pc WHERE pc.componentProductId = :productId")
    void deleteByComponentProductId(@Param("productId") UUID productId);

    @Query(value = """
        SELECT DISTINCT BIN_TO_UUID(p.id) FROM (
            SELECT product_id AS id FROM order_lines WHERE product_id IN :ids
            UNION ALL
            SELECT product_id FROM warehouse_stock WHERE product_id IN :ids
            UNION ALL
            SELECT product_id FROM stock_movements WHERE product_id IN :ids
            UNION ALL
            SELECT product_id FROM invoice_lines WHERE product_id IN :ids
            UNION ALL
            SELECT product_id FROM return_lines WHERE product_id IN :ids
            UNION ALL
            SELECT component_product_id AS id FROM product_components WHERE component_product_id IN :ids
        ) p
        """, nativeQuery = true)
    Set<UUID> findProductIdsWithReferences(@Param("ids") Collection<UUID> ids);

    @Query("""
        SELECT p FROM ProductEntity p
        WHERE p.active = true
        AND (:search IS NULL
               OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:categoryId IS NULL OR p.categoryId = :categoryId)
        AND (:composite IS NULL OR p.composite = :composite)
        AND (:withStock IS NULL
               OR (:withStock = true AND EXISTS (
                   SELECT s FROM WarehouseStockEntity s
                   WHERE s.productId = p.id
                   AND s.warehouseId IN :allowedWarehouseIds
                   AND (s.quantity - s.reserved) > 0
               ))
               OR (:withStock = false AND NOT EXISTS (
                   SELECT s FROM WarehouseStockEntity s
                   WHERE s.productId = p.id
                   AND s.warehouseId IN :allowedWarehouseIds
                   AND (s.quantity - s.reserved) > 0
               )))
        """)
    Page<ProductEntity> findAllForOrder(
            @Param("search") String search,
            @Param("categoryId") UUID categoryId,
            @Param("composite") Boolean composite,
            @Param("withStock") Boolean withStock,
            @Param("allowedWarehouseIds") Collection<UUID> allowedWarehouseIds,
            Pageable pageable);
}
