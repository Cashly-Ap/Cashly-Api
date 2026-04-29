package com.parsun.parsun_api.warehouses.infrastructure.persistence.repositories;

import com.parsun.parsun_api.warehouses.infrastructure.persistence.entities.WarehouseStockEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataWarehouseStockRepository extends JpaRepository<WarehouseStockEntity, UUID> {

    Optional<WarehouseStockEntity> findByWarehouseIdAndProductId(UUID warehouseId, UUID productId);

    Page<WarehouseStockEntity> findByWarehouseId(UUID warehouseId, Pageable pageable);

    boolean existsByProductId(UUID productId);

    @Modifying
    @Query("DELETE FROM WarehouseStockEntity w WHERE w.productId = :productId")
    void deleteByProductId(@Param("productId") UUID productId);
}
