package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.inventory.warehouses.management.infrastructure.persistence.entities.WarehouseEntity;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.ReferenceType;
import com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.entities.StockMovementEntity;
import com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.entities.WarehouseStockEntity;
import com.cashly.cashly_api.partners.customers.warehouse_access.infrastructure.persistence.entities.CustomerWarehouseAccessEntity;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V09WarehousesSeed implements SeedStep {
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2025, 1, 6, 8, 0);
    private final EntityManager entityManager;
    public V09WarehousesSeed(EntityManager entityManager) { this.entityManager = entityManager; }
    @Override public int version() { return 9; }

    @Override public void seed() {
        warehouse(WAREHOUSE_MAIN, "MAIN", "Main warehouse");
        warehouse(WAREHOUSE_RESERVE, "RESERVE", "Reserve warehouse");
        stock("main-coffee", WAREHOUSE_MAIN, PRODUCT_COFFEE, 100);
        stock("main-water", WAREHOUSE_MAIN, PRODUCT_WATER, 150);
        stock("reserve-coffee", WAREHOUSE_RESERVE, PRODUCT_COFFEE, 40);
        movement("coffee-entry", WAREHOUSE_MAIN, PRODUCT_COFFEE, 100);
        movement("water-entry", WAREHOUSE_MAIN, PRODUCT_WATER, 150);
        access("customer-one-main", CUSTOMER_ONE, WAREHOUSE_MAIN);
        access("customer-two-main", CUSTOMER_TWO, WAREHOUSE_MAIN);
    }

    private void warehouse(UUID id, String code, String name) {
        if (entityManager.find(WarehouseEntity.class, id) != null) return;
        var entity = new WarehouseEntity(); entity.setId(id); entity.setCode(code); entity.setName(name);
        entity.setAddress("Avenida Principal"); entity.setState("Distrito Capital"); entity.setMunicipality("Libertador");
        entity.setPostalCode("1010"); entity.setActive(true); entity.setCreatedAt(CREATED_AT); entity.setUpdatedAt(CREATED_AT);
        entityManager.persist(entity);
    }

    private void stock(String key, UUID warehouseId, UUID productId, int quantity) {
        UUID id = id("warehouse-stock." + key); if (entityManager.find(WarehouseStockEntity.class, id) != null) return;
        var entity = new WarehouseStockEntity(); entity.setId(id); entity.setWarehouseId(warehouseId); entity.setProductId(productId);
        entity.setQuantity(quantity); entity.setReserved(0); entity.setLastMovementAt(CREATED_AT); entity.setUpdatedAt(CREATED_AT);
        entityManager.persist(entity);
    }

    private void movement(String key, UUID warehouseId, UUID productId, int quantity) {
        UUID id = id("stock-movement." + key); if (entityManager.find(StockMovementEntity.class, id) != null) return;
        var entity = new StockMovementEntity(); entity.setId(id); entity.setWarehouseId(warehouseId); entity.setProductId(productId);
        entity.setMovementType(MovementType.ENTRY); entity.setQuantity(quantity); entity.setPreviousStock(0); entity.setNewStock(quantity);
        entity.setReferenceType(ReferenceType.MANUAL); entity.setReferenceNumber("SEED"); entity.setNotes("Opening stock"); entity.setCreatedAt(CREATED_AT);
        entityManager.persist(entity);
    }

    private void access(String key, UUID customerId, UUID warehouseId) {
        UUID id = id("warehouse-access." + key); if (entityManager.find(CustomerWarehouseAccessEntity.class, id) != null) return;
        var entity = new CustomerWarehouseAccessEntity(); entity.setId(id); entity.setCustomerId(customerId); entity.setWarehouseId(warehouseId);
        entity.setGrantedAt(CREATED_AT); entity.setGrantedBy(ADMIN_USER); entityManager.persist(entity);
    }
}
