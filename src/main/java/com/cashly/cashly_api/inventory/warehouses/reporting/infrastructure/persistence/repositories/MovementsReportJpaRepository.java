package com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.persistence.repositories;

import com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.persistence.projections.MovementsReportProjection;

import com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.entities.StockMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MovementsReportJpaRepository
        extends
            JpaRepository<StockMovementEntity, UUID> {

    @Query(value = """
            SELECT
              BIN_TO_UUID(sm.id) as movementId,
              w.name as warehouseName,
              p.name as productName,
              p.code as sku,
              sm.movement_type as movementType,
              sm.quantity as quantity,
              sm.previous_stock as previousStock,
              sm.new_stock as newStock,
              sm.notes as notes,
              sm.created_at as createdAt
            FROM stock_movements sm
            JOIN warehouses w ON w.id = sm.warehouse_id
            JOIN products p ON p.id = sm.product_id
            WHERE (:warehouseId IS NULL OR sm.warehouse_id = UNHEX(REPLACE(:warehouseId, '-', '')))
              AND (:productId IS NULL OR sm.product_id = UNHEX(REPLACE(:productId, '-', '')))
              AND (:movementType IS NULL OR sm.movement_type = :movementType)
              AND (:dateFrom IS NULL OR DATE(sm.created_at) >= :dateFrom)
              AND (:dateTo IS NULL OR DATE(sm.created_at) <= :dateTo)
            ORDER BY sm.created_at DESC
            """, nativeQuery = true)
    List<MovementsReportProjection> findAllForPdf(
            @Param("warehouseId") String warehouseId,
            @Param("productId") String productId,
            @Param("movementType") String movementType,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo);
}
