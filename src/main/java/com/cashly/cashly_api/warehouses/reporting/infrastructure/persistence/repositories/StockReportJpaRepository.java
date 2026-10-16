package com.cashly.cashly_api.warehouses.reporting.infrastructure.persistence.repositories;

import com.cashly.cashly_api.warehouses.reporting.infrastructure.persistence.projections.StockReportProjection;

import com.cashly.cashly_api.warehouses.stock.infrastructure.persistence.entities.WarehouseStockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StockReportJpaRepository extends JpaRepository<WarehouseStockEntity, UUID> {

    @Query(value = """
            SELECT
              w.name as warehouseName,
              p.name as productName,
              p.code as sku,
              c.name as categoryName,
              ws.quantity as quantity
            FROM warehouse_stock ws
            JOIN products p ON p.id = ws.product_id
            JOIN categories c ON c.id = p.category_id
            JOIN warehouses w ON w.id = ws.warehouse_id
            WHERE (:warehouseId IS NULL OR ws.warehouse_id = UNHEX(REPLACE(:warehouseId, '-', '')))
              AND (:categoryId IS NULL OR p.category_id = UNHEX(REPLACE(:categoryId, '-', '')))
              AND (:withStock IS NULL OR (:withStock = TRUE AND ws.quantity > 0) OR (:withStock = FALSE AND ws.quantity = 0))
            ORDER BY w.name, p.name
            """,
            nativeQuery = true)
    List<StockReportProjection> findAllForPdf(
            @Param("warehouseId") String warehouseId,
            @Param("categoryId") String categoryId,
            @Param("withStock") Boolean withStock,
            @Param("lowStock") Boolean lowStock);
}
