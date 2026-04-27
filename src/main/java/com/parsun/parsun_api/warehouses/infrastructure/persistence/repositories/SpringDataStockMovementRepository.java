package com.parsun.parsun_api.warehouses.infrastructure.persistence.repositories;

import com.parsun.parsun_api.warehouses.infrastructure.persistence.entities.StockMovementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataStockMovementRepository extends JpaRepository<StockMovementEntity, UUID> {

    Page<StockMovementEntity> findByWarehouseIdAndProductId(UUID warehouseId, UUID productId, Pageable pageable);

    Page<StockMovementEntity> findByWarehouseId(UUID warehouseId, Pageable pageable);
}
