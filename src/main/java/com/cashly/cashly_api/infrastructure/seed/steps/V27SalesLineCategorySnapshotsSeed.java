package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.id;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.sales.orders.infrastructure.persistence.entities.OrderLineEntity;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteLineJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

@Component
public class V27SalesLineCategorySnapshotsSeed implements SeedStep {
    private final EntityManager entityManager;
    public V27SalesLineCategorySnapshotsSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    public int version() {
        return 27;
    }
    @Override
    public void seed() {
        verifyOrderSnapshot("order-line.one");
        verifyOrderSnapshot("order-line.two");
        verifyQuoteSnapshot("quote-line.one");
        verifyQuoteSnapshot("quote-line.two");
    }
    private void verifyOrderSnapshot(String key) {
        var line = entityManager.find(OrderLineEntity.class, id(key));
        if (line == null || line.getCategoryIdSnapshot() == null)
            throw new IllegalStateException(
                    "Missing order category snapshot for " + key);
    }
    private void verifyQuoteSnapshot(String key) {
        var line = entityManager.find(QuoteLineJpaEntity.class, id(key));
        if (line == null || line.getCategoryIdSnapshot() == null)
            throw new IllegalStateException(
                    "Missing quote category snapshot for " + key);
    }
}
