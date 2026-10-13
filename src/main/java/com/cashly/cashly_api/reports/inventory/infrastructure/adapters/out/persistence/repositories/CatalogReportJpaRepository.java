package com.cashly.cashly_api.reports.inventory.infrastructure.adapters.out.persistence.repositories;

import com.cashly.cashly_api.reports.inventory.infrastructure.adapters.out.persistence.projections.CatalogReportProjection;

import com.cashly.cashly_api.warehouses.infrastructure.adapters.out.persistence.entities.WarehouseStockEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CatalogReportJpaRepository extends JpaRepository<WarehouseStockEntity, UUID> {

    @Query(value = """
            SELECT
              BIN_TO_UUID(p.id) as productId,
              p.name as productName,
              p.code as sku,
              BIN_TO_UUID(c.id) as categoryId,
              c.name as categoryName,
              p.composite as composite,
              BIN_TO_UUID(w.id) as warehouseId,
              w.name as warehouseName,
              ws.quantity as quantity,
              p.active as active
            FROM warehouse_stock ws
            JOIN products p ON p.id = ws.product_id
            JOIN categories c ON c.id = p.category_id
            JOIN warehouses w ON w.id = ws.warehouse_id
            WHERE (:search IS NULL OR p.name LIKE %:search%)
              AND (:categoryId IS NULL OR p.category_id = UNHEX(REPLACE(:categoryId, '-', '')))
              AND (:warehouseId IS NULL OR ws.warehouse_id = UNHEX(REPLACE(:warehouseId, '-', '')))
              AND (:composite IS NULL OR p.composite = :composite)
              AND (:withStock IS NULL OR (:withStock = TRUE AND ws.quantity > 0) OR (:withStock = FALSE))
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM warehouse_stock ws
            JOIN products p ON p.id = ws.product_id
            JOIN categories c ON c.id = p.category_id
            JOIN warehouses w ON w.id = ws.warehouse_id
            WHERE (:search IS NULL OR p.name LIKE %:search%)
              AND (:categoryId IS NULL OR p.category_id = UNHEX(REPLACE(:categoryId, '-', '')))
              AND (:warehouseId IS NULL OR ws.warehouse_id = UNHEX(REPLACE(:warehouseId, '-', '')))
              AND (:composite IS NULL OR p.composite = :composite)
              AND (:withStock IS NULL OR (:withStock = TRUE AND ws.quantity > 0) OR (:withStock = FALSE))
            """,
            nativeQuery = true)
    Page<CatalogReportProjection> findCatalog(
            @Param("search") String search,
            @Param("categoryId") String categoryId,
            @Param("warehouseId") String warehouseId,
            @Param("composite") Boolean composite,
            @Param("withStock") Boolean withStock,
            Pageable pageable);
}
