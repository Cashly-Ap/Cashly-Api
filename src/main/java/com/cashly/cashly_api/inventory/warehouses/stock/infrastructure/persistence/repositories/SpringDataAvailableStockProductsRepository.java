package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.repositories;

import com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.entities.WarehouseStockEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import java.util.Set;
import java.util.UUID;

public interface SpringDataAvailableStockProductsRepository
        extends
            Repository<WarehouseStockEntity, UUID> {
    @Query("""
            SELECT DISTINCT s.productId FROM WarehouseStockEntity s
            WHERE s.warehouseId IN :warehouseIds AND (s.quantity - s.reserved) > 0
            """)
    Set<UUID> findAvailableProductIds(
            @Param("warehouseIds") Set<UUID> warehouseIds);
}