package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteAllocationJpaEntity;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteJpaEntity;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteLineJpaEntity;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteNumberSequenceJpaEntity;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V15QuotesSeed implements SeedStep {

    private static final Instant CREATED_AT = Instant
            .parse("2025-01-09T12:00:00Z");
    private final EntityManager entityManager;

    public V15QuotesSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public int version() {
        return 15;
    }

    @Override
    public void seed() {
        quote(QUOTE_ONE, "QUO-2025-0001", CUSTOMER_ONE, "Mercado Central, C.A.",
                "J-10000001-1", "one", PRODUCT_COFFEE, "Ground coffee",
                "PRD-0001", "4", "6.50", CATEGORY_DRINKS);
        quote(QUOTE_TWO, "QUO-2025-0002", CUSTOMER_TWO, "Ana Pérez",
                "V-10000002-2", "two", PRODUCT_WATER, "Mineral water",
                "PRD-0002", "6", "1.00", CATEGORY_DRINKS);
        if (entityManager.find(QuoteNumberSequenceJpaEntity.class,
                2025) == null) {
            var sequence = new QuoteNumberSequenceJpaEntity();
            sequence.setYear(2025);
            sequence.setNextValue(3);
            entityManager.persist(sequence);
        }
    }

    private void quote(UUID id, String number, UUID customerId,
            String customerName, String rif, String key, UUID productId,
            String productName, String code, String quantity, String price,
            UUID categoryId) {
        if (entityManager.find(QuoteJpaEntity.class, id) != null) {
            return;
        }
        BigDecimal qty = new BigDecimal(quantity),
                unitPrice = new BigDecimal(price),
                total = qty.multiply(unitPrice);
        var quote = new QuoteJpaEntity();
        quote.setId(id);
        quote.setQuoteNumber(number);
        quote.setCustomerId(customerId);
        quote.setCustomerName(customerName);
        quote.setCustomerRif(rif);
        quote.setCreatedBy(OPERATOR_USER);
        quote.setCreatedByName("Sales Operator");
        quote.setStatus("DRAFT");
        quote.setExpiryDate(LocalDate.of(2025, 2, 9));
        quote.setNotes("Deterministic seed quote");
        quote.setSubtotal(total);
        quote.setCreatedAt(CREATED_AT);
        quote.setUpdatedAt(CREATED_AT);
        var line = new QuoteLineJpaEntity();
        line.setId(id("quote-line." + key));
        line.setQuote(quote);
        line.setProductId(productId);
        line.setProductName(productName);
        line.setProductCode(code);
        line.setQuantity(qty);
        line.setUnitPrice(unitPrice);
        line.setLineTotal(total);
        line.setCategorySnapshot(categoryId);
        var allocation = new QuoteAllocationJpaEntity();
        allocation.setId(id("quote-allocation." + key));
        allocation.setQuoteLine(line);
        allocation.setQuoteId(id);
        allocation.setProductId(productId);
        allocation.setWarehouseId(WAREHOUSE_MAIN);
        allocation.setWarehouseName("Main warehouse");
        allocation.setAllocatedQuantity(qty);
        line.getAllocations().add(allocation);
        quote.getLines().add(line);
        entityManager.persist(quote);
    }
}
