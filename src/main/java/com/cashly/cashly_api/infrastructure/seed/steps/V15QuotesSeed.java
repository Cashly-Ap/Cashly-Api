package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;
import static com.cashly.cashly_api.infrastructure.seed.SeedDocuments.*;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.infrastructure.seed.SeedDocuments;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteAllocationJpaEntity;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteJpaEntity;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteLineJpaEntity;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteNumberSequenceJpaEntity;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteStatus;
import java.time.ZoneOffset;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V15QuotesSeed implements SeedStep {

    private static final Instant CREATED_AT = Instant
            .parse("2026-01-09T12:00:00Z");
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
        quote(QUOTE_ONE, "PRE-2026-0001", CUSTOMER_ONE, "Mercado Central, C.A.",
                "J-10000001-1", "one", PRODUCT_COFFEE, "Ground coffee",
                "PRD-0001", "4", "6.50", CATEGORY_DRINKS, CREATED_AT);
        quote(QUOTE_TWO, "PRE-2026-0002", CUSTOMER_TWO, "Ana Pérez",
                "V-10000002-2", "two", PRODUCT_WATER, "Mineral water",
                "PRD-0002", "6", "1.00", CATEGORY_DRINKS, CREATED_AT);
        for (var sample : SeedDocuments.additional()) {
            quote(id("quote." + sample.key()), sample.documentNumber("PRE"),
                    sample.customerId(), sample.customerName(),
                    sample.customerRif(), sample.key(), sample.productId(),
                    sample.productName(), sample.productCode(),
                    sample.quantity(), sample.price(), CATEGORY_DRINKS,
                    sample.createdAt(9, 0).toInstant(ZoneOffset.UTC));
            applyScenario(sample, sample.scenario());
        }
        for (int year : SeedDocuments.years()) {
            var sequence = entityManager.find(QuoteNumberSequenceJpaEntity.class,
                    year);
            int nextValue = SeedDocuments.nextNumberFor(year);
            if (sequence == null) {
                sequence = new QuoteNumberSequenceJpaEntity();
                sequence.setYear(year);
                sequence.setNextValue(nextValue);
                entityManager.persist(sequence);
            } else if (sequence.getNextValue() < nextValue) {
                sequence.setNextValue(nextValue);
            }
        }
    }

    private void quote(UUID id, String number, UUID customerId,
            String customerName, String rif, String key, UUID productId,
            String productName, String code, String quantity, String price,
            UUID categoryId, Instant createdAt) {
        var existing = entityManager.find(QuoteJpaEntity.class, id);
        if (existing != null) {
            if (existing.getQuoteNumber()
                    .equals(number.replace("PRE-", "QUO-"))) {
                existing.setQuoteNumber(number);
            }
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
        quote.setExpiryDate(LocalDate.ofInstant(createdAt,
                ZoneOffset.UTC).plusDays(31));
        quote.setNotes("Deterministic seed quote");
        quote.setSubtotal(total);
        quote.setCreatedAt(createdAt);
        quote.setUpdatedAt(createdAt);
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

    private void applyScenario(SeedDocuments.Sample sample, Scenario scenario) {
        var quote = entityManager.find(QuoteJpaEntity.class,
                id("quote." + sample.key()));
        if (quote == null
                || !"Deterministic seed quote".equals(quote.getNotes())) {
            return;
        }
        quote.setStatus(scenario.quote().name());
        quote.setOrderId(scenario.quote() == QuoteStatus.CONVERTED_TO_ORDER
                ? id("order." + sample.key())
                : null);
        quote.setOrderNumber(scenario.quote() == QuoteStatus.CONVERTED_TO_ORDER
                ? sample.documentNumber("ORD")
                : null);
        quote.setInvoiceId(scenario.quote() == QuoteStatus.CONVERTED_TO_INVOICE
                ? id("invoice." + sample.key())
                : null);
        quote.setInvoiceNumber(
                scenario.quote() == QuoteStatus.CONVERTED_TO_INVOICE
                        ? sample.documentNumber("INV")
                        : null);
        quote.setCancelledAt(scenario.quote() == QuoteStatus.CANCELLED
                ? sample.createdAt(10, 0).toInstant(ZoneOffset.UTC)
                : null);
        quote.setCancelledReason(scenario.quote() == QuoteStatus.CANCELLED
                ? CANCELLATION_REASON
                : null);
        quote.setNotes(SCENARIO_NOTES);
        quote.setUpdatedAt(
                sample.createdAt(10, 0).toInstant(ZoneOffset.UTC));
    }

}
