package com.cashly.cashly_api.warehouses.infrastructure.adapters.out.persistence.repositories;

import com.cashly.cashly_api.warehouses.infrastructure.adapters.out.persistence.entities.WarehouseStockEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import java.util.Set;
import java.util.UUID;

public interface SpringDataWarehousesProductReferenceRepository extends Repository<WarehouseStockEntity, UUID> {
    @Query(value = "SELECT DISTINCT BIN_TO_UUID(product_id) FROM warehouse_stock WHERE product_id IN :ids UNION SELECT DISTINCT BIN_TO_UUID(product_id) FROM stock_movements WHERE product_id IN :ids", nativeQuery = true)
    Set<UUID> findReferencedProductIds(@Param("ids") Set<UUID> ids);
}