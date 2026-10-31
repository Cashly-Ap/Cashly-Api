package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.infrastructure.seed.SeedDocuments;
import com.cashly.cashly_api.sales.orders.infrastructure.persistence.entities.OrderLineAllocationEntity;
import com.cashly.cashly_api.sales.orders.infrastructure.persistence.entities.OrderLineEntity;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class V12OrderAllocationsSeed implements SeedStep {
    private final EntityManager entityManager;
    public V12OrderAllocationsSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    public int version() {
        return 12;
    }
    @Override
    public void seed() {
        allocation("one", "order-line.one", WAREHOUSE_MAIN, "2");
        allocation("two", "order-line.two", WAREHOUSE_MAIN, "3");
        for (var sample : SeedDocuments.additional()) {
            allocation(sample.key(), "order-line." + sample.key(),
                    WAREHOUSE_MAIN, sample.quantity());
        }
    }
    private void allocation(String key, String lineKey,
            java.util.UUID warehouseId, String quantity) {
        var allocationId = id("order-allocation." + key);
        if (entityManager.find(OrderLineAllocationEntity.class,
                allocationId) != null)
            return;
        var allocation = new OrderLineAllocationEntity();
        allocation.setId(allocationId);
        allocation.setOrderLine(
                entityManager.getReference(OrderLineEntity.class, id(lineKey)));
        allocation.setWarehouseId(warehouseId);
        allocation.setAllocated(new BigDecimal(quantity));
        entityManager.persist(allocation);
    }
}
