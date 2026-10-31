package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;
import static com.cashly.cashly_api.infrastructure.seed.SeedDocuments.*;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.infrastructure.seed.SeedDocuments;
import com.cashly.cashly_api.sales.orders.infrastructure.persistence.entities.OrderEntity;
import com.cashly.cashly_api.sales.orders.infrastructure.persistence.entities.OrderLineEntity;
import com.cashly.cashly_api.sales.orders.infrastructure.persistence.entities.OrderNumberSequenceEntity;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderStatus;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V11OrdersSeed implements SeedStep {
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2025, 1, 7,
            8, 0);
    private final EntityManager entityManager;
    public V11OrdersSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    public int version() {
        return 11;
    }
    @Override
    public void seed() {
        order(ORDER_ONE, "ORD-2025-0001", CUSTOMER_ONE, "Mercado Central, C.A.",
                "J-10000001-1", id("order-line.one"), PRODUCT_COFFEE,
                "Ground coffee", "PRD-0001", "2", "6.50", CATEGORY_DRINKS);
        order(ORDER_TWO, "ORD-2025-0002", CUSTOMER_TWO, "Ana Pérez",
                "V-10000002-2", id("order-line.two"), PRODUCT_WATER,
                "Mineral water", "PRD-0002", "3", "1.00", CATEGORY_DRINKS);
        for (var sample : SeedDocuments.additional()) {
            order(id("order." + sample.key()), sample.documentNumber("ORD"),
                    sample.customerId(), sample.customerName(),
                    sample.customerRif(), id("order-line." + sample.key()),
                    sample.productId(), sample.productName(),
                    sample.productCode(), sample.quantity(), sample.price(),
                    CATEGORY_DRINKS);
            applyScenario(sample, sample.scenario());
        }
        var sequence = entityManager.find(OrderNumberSequenceEntity.class,
                SeedDocuments.YEAR);
        if (sequence == null) {
            sequence = new OrderNumberSequenceEntity();
            sequence.setYear(SeedDocuments.YEAR);
            sequence.setNextValue(SeedDocuments.NEXT_NUMBER);
            entityManager.persist(sequence);
        } else if (sequence.getNextValue() < SeedDocuments.NEXT_NUMBER) {
            sequence.setNextValue(SeedDocuments.NEXT_NUMBER);
        }
    }
    private void order(UUID id, String number, UUID customerId,
            String customerName, String rif, UUID lineId, UUID productId,
            String productName, String code, String quantity, String price,
            UUID categoryId) {
        if (entityManager.find(OrderEntity.class, id) != null)
            return;
        var order = new OrderEntity();
        order.setId(id);
        order.setOrderNumber(number);
        order.setCustomerId(customerId);
        order.setCustomerName(customerName);
        order.setCustomerRif(rif);
        order.setCreatedBy(OPERATOR_USER);
        order.setCreatedByName("Sales Operator");
        order.setStatus("CONFIRMED");
        order.setNotes("Deterministic seed order");
        BigDecimal qty = new BigDecimal(quantity),
                unitPrice = new BigDecimal(price);
        order.setSubtotal(qty.multiply(unitPrice));
        order.setCreatedAt(CREATED_AT);
        order.setUpdatedAt(CREATED_AT);
        var line = new OrderLineEntity();
        line.setId(lineId);
        line.setOrder(order);
        line.setProductId(productId);
        line.setProductName(productName);
        line.setProductCode(code);
        line.setQuantity(qty);
        line.setUnitPrice(unitPrice);
        line.setLineTotal(qty.multiply(unitPrice));
        line.setCategorySnapshot(categoryId);
        order.getLines().add(line);
        entityManager.persist(order);
    }

    private void applyScenario(SeedDocuments.Sample sample, Scenario scenario) {
        var order = entityManager.find(OrderEntity.class,
                id("order." + sample.key()));
        if (order == null
                || !"Deterministic seed order".equals(order.getNotes())) {
            return;
        }
        order.setStatus(scenario.order().name());
        order.setInvoiceId(scenario.order() == OrderStatus.INVOICED
                ? id("invoice." + sample.key())
                : null);
        order.setInvoiceNumber(scenario.order() == OrderStatus.INVOICED
                ? sample.documentNumber("INV")
                : null);
        order.setCancelledAt(
                scenario.order() == OrderStatus.CANCELLED ? EVENT_AT : null);
        order.setCancelledReason(scenario.order() == OrderStatus.CANCELLED
                ? CANCELLATION_REASON
                : null);
        order.setNotes(SCENARIO_NOTES);
        order.setUpdatedAt(EVENT_AT);
    }

}
