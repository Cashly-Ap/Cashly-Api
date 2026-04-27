package com.parsun.parsun_api.reports.inventory.infrastructure.persistence;

import com.parsun.parsun_api.warehouses.infrastructure.persistence.entities.WarehouseStockEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CatalogReportJpaRepository extends JpaRepository<WarehouseStockEntity, Long> {

    @Query(value = """
            SELECT
              p.id as productId,
              p.name as productName,
              p.code as sku,
              c.id as categoryId,
              c.name as categoryName,
              p.composite as composite,
              w.id as warehouseId,
              w.name as warehouseName,
              ws.quantity as quantity,
              ws.min_stock as minStock,
              p.active as active
            FROM warehouse_stock ws
            JOIN products p ON p.id = ws.product_id
            JOIN categories c ON c.id = p.category_id
            JOIN warehouses w ON w.id = ws.warehouse_id
            WHERE (:search IS NULL OR p.name LIKE %:search%)
              AND (:categoryId IS NULL OR p.category_id = :categoryId)
              AND (:warehouseId IS NULL OR ws.warehouse_id = :warehouseId)
              AND (:composite IS NULL OR p.composite = :composite)
              AND (:withStock IS NULL OR (:withStock = TRUE AND ws.quantity > 0) OR (:withStock = FALSE))
              AND (:lowStock IS NULL OR (:lowStock = TRUE AND ws.quantity <= ws.min_stock) OR (:lowStock = FALSE))
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM warehouse_stock ws
            JOIN products p ON p.id = ws.product_id
            JOIN categories c ON c.id = p.category_id
            JOIN warehouses w ON w.id = ws.warehouse_id
            WHERE (:search IS NULL OR p.name LIKE %:search%)
              AND (:categoryId IS NULL OR p.category_id = :categoryId)
              AND (:warehouseId IS NULL OR ws.warehouse_id = :warehouseId)
              AND (:composite IS NULL OR p.composite = :composite)
              AND (:withStock IS NULL OR (:withStock = TRUE AND ws.quantity > 0) OR (:withStock = FALSE))
              AND (:lowStock IS NULL OR (:lowStock = TRUE AND ws.quantity <= ws.min_stock) OR (:lowStock = FALSE))
            """,
            nativeQuery = true)
    Page<CatalogReportProjection> findCatalog(
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("warehouseId") Long warehouseId,
            @Param("composite") Boolean composite,
            @Param("withStock") Boolean withStock,
            @Param("lowStock") Boolean lowStock,
            Pageable pageable);
}
