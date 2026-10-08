package com.cashly.cashly_api.warehouses.infrastructure.persistence.repositories;

import com.cashly.cashly_api.warehouses.infrastructure.persistence.entities.WarehouseStockEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SpringDataWarehouseStockRepository extends JpaRepository<WarehouseStockEntity, UUID> {

    Optional<WarehouseStockEntity> findByWarehouseIdAndProductId(UUID warehouseId, UUID productId);

    boolean existsByWarehouseIdAndProductId(UUID warehouseId, UUID productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WarehouseStockEntity w WHERE w.warehouseId = :warehouseId AND w.productId = :productId")
    Optional<WarehouseStockEntity> findByWarehouseIdAndProductIdForUpdate(@Param("warehouseId") UUID warehouseId, @Param("productId") UUID productId);

    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT INTO warehouse_stock (id, warehouse_id, product_id, quantity, reserved, last_movement_at, updated_at)
            VALUES (UNHEX(REPLACE(:id, '-', '')), UNHEX(REPLACE(:warehouseId, '-', '')),
                    UNHEX(REPLACE(:productId, '-', '')), 0, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
            ON DUPLICATE KEY UPDATE quantity = quantity
            """, nativeQuery = true)
    int insertIfMissing(@Param("id") String id, @Param("warehouseId") String warehouseId,
                        @Param("productId") String productId);

    Page<WarehouseStockEntity> findByWarehouseId(UUID warehouseId, Pageable pageable);

    List<WarehouseStockEntity> findByProductId(UUID productId);

    List<WarehouseStockEntity> findAllByProductIdIn(Set<UUID> productIds);

    boolean existsByProductId(UUID productId);

    @Modifying
    @Query("DELETE FROM WarehouseStockEntity w WHERE w.productId = :productId")
    void deleteByProductId(@Param("productId") UUID productId);
}
