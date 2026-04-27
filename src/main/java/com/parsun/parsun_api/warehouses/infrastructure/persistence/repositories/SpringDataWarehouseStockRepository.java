package com.parsun.parsun_api.warehouses.infrastructure.persistence.repositories;

import com.parsun.parsun_api.warehouses.infrastructure.persistence.entities.WarehouseStockEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataWarehouseStockRepository extends JpaRepository<WarehouseStockEntity, UUID> {

    Optional<WarehouseStockEntity> findByWarehouseIdAndProductId(UUID warehouseId, UUID productId);

    Page<WarehouseStockEntity> findByWarehouseId(UUID warehouseId, Pageable pageable);

    boolean existsByProductId(UUID productId);
}
