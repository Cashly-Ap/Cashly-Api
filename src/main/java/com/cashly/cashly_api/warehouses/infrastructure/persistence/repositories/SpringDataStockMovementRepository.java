package com.cashly.cashly_api.warehouses.infrastructure.persistence.repositories;

import com.cashly.cashly_api.warehouses.infrastructure.persistence.entities.StockMovementEntity;
import com.cashly.cashly_api.warehouses.domain.entities.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface SpringDataStockMovementRepository extends JpaRepository<StockMovementEntity, UUID> {

    Page<StockMovementEntity> findByWarehouseIdAndProductId(UUID warehouseId, UUID productId, Pageable pageable);

    Page<StockMovementEntity> findByWarehouseIdAndMovementType(
            UUID warehouseId, MovementType movementType, Pageable pageable);

    Page<StockMovementEntity> findByWarehouseIdAndProductIdAndMovementType(
            UUID warehouseId, UUID productId, MovementType movementType, Pageable pageable);

    Page<StockMovementEntity> findByWarehouseId(UUID warehouseId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM StockMovementEntity s WHERE s.productId = :productId")
    void deleteByProductId(@Param("productId") UUID productId);
}
