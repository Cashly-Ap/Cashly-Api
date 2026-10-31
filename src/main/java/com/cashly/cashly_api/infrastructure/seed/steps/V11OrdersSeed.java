package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.sales.orders.infrastructure.persistence.entities.OrderEntity;
import com.cashly.cashly_api.sales.orders.infrastructure.persistence.entities.OrderLineEntity;
import com.cashly.cashly_api.sales.orders.infrastructure.persistence.entities.OrderNumberSequenceEntity;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V11OrdersSeed implements SeedStep {
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2025, 1, 7, 8, 0);
    private final EntityManager entityManager;
    public V11OrdersSeed(EntityManager entityManager) { this.entityManager = entityManager; }
    @Override public int version() { return 11; }
    @Override public void seed() {
        order(ORDER_ONE, "ORD-2025-0001", CUSTOMER_ONE, "Mercado Central, C.A.", "J-10000001-1",
            id("order-line.one"), PRODUCT_COFFEE, "Ground coffee", "PRD-0001", "2", "6.50", CATEGORY_DRINKS);
        order(ORDER_TWO, "ORD-2025-0002", CUSTOMER_TWO, "Ana Pérez", "V-10000002-2",
            id("order-line.two"), PRODUCT_WATER, "Mineral water", "PRD-0002", "3", "1.00", CATEGORY_DRINKS);
        if (entityManager.find(OrderNumberSequenceEntity.class, 2025) == null) {
            var sequence = new OrderNumberSequenceEntity(); sequence.setYear(2025); sequence.setNextValue(3); entityManager.persist(sequence);
        }
    }
    private void order(UUID id, String number, UUID customerId, String customerName, String rif, UUID lineId,
                       UUID productId, String productName, String code, String quantity, String price, UUID categoryId) {
        if (entityManager.find(OrderEntity.class, id) != null) return;
        var order = new OrderEntity(); order.setId(id); order.setOrderNumber(number); order.setCustomerId(customerId);
        order.setCustomerName(customerName); order.setCustomerRif(rif); order.setCreatedBy(OPERATOR_USER); order.setCreatedByName("Sales Operator");
        order.setStatus("CONFIRMED"); order.setNotes("Deterministic seed order");
        BigDecimal qty = new BigDecimal(quantity), unitPrice = new BigDecimal(price); order.setSubtotal(qty.multiply(unitPrice));
        order.setCreatedAt(CREATED_AT); order.setUpdatedAt(CREATED_AT);
        var line = new OrderLineEntity(); line.setId(lineId); line.setOrder(order); line.setProductId(productId); line.setProductName(productName);
        line.setProductCode(code); line.setQuantity(qty); line.setUnitPrice(unitPrice); line.setLineTotal(qty.multiply(unitPrice));
        line.setCategorySnapshot(categoryId); order.getLines().add(line); entityManager.persist(order);
    }
}
