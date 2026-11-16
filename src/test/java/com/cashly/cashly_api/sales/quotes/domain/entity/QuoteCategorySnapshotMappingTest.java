package com.cashly.cashly_api.sales.quotes.domain.entity;

import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteJpaEntity;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.mappers.QuoteJpaMapper;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteLineJpaEntity;

import com.cashly.cashly_api.sales.quotes.domain.entity.Quote;
import com.cashly.cashly_api.sales.quotes.domain.entity.QuoteLine;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteNumber;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class QuoteCategorySnapshotMappingTest {
    private final QuoteJpaMapper mapper = new QuoteJpaMapper();

    @Test
    void mapsHistoricalCategoryThroughPersistence() {
        UUID categoryId = UUID.randomUUID();
        Quote quote = quote(new QuoteLine(UUID.randomUUID(), "Product", "SKU",
                categoryId, BigDecimal.ONE, BigDecimal.TEN));

        QuoteJpaEntity persisted = mapper.toJpa(quote);
        QuoteLineJpaEntity line = persisted.getLines().getFirst();
        Quote restored = mapper.toDomain(persisted);

        assertEquals(categoryId, line.getCategoryIdSnapshot());
        assertEquals(categoryId,
                restored.getLines().getFirst().getCategoryId());
    }

    @Test
    void keepsLegacyCategorySnapshotAbsent() {
        Quote quote = quote(new QuoteLine(UUID.randomUUID(), "Product", "SKU",
                BigDecimal.ONE, BigDecimal.TEN));

        assertNull(mapper.toJpa(quote).getLines().getFirst()
                .getCategoryIdSnapshot());
    }

    private Quote quote(QuoteLine line) {
        return Quote.reconstitute(UUID.randomUUID(), QuoteNumber.of(2026, 1),
                UUID.randomUUID(), "Customer", "RIF", UUID.randomUUID(),
                "Actor", QuoteStatus.DRAFT, LocalDate.of(2026, 12, 31), null,
                BigDecimal.TEN, null, null, null, null, null, null,
                List.of(line), Instant.now(), Instant.now());
    }
}
