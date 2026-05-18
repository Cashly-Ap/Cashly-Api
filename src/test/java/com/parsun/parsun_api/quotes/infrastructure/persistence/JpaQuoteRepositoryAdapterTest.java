package com.parsun.parsun_api.quotes.infrastructure.persistence;

import com.parsun.parsun_api.integration.BaseIntegrationTest;
import com.parsun.parsun_api.quotes.domain.entities.Quote;
import com.parsun.parsun_api.quotes.domain.entities.QuoteAllocation;
import com.parsun.parsun_api.quotes.domain.entities.QuoteLine;
import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteNumber;
import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JpaQuoteRepositoryAdapter Integration Tests")
class JpaQuoteRepositoryAdapterTest extends BaseIntegrationTest {

    @Autowired
    private JpaQuoteRepositoryAdapter repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID testCustomerId;

    @BeforeEach
    void setUp() {
        // Clean up any leftover data from previous runs
        jdbcTemplate.update("DELETE FROM quote_allocations");
        jdbcTemplate.update("DELETE FROM quote_lines");
        jdbcTemplate.update("DELETE FROM quotes");
        jdbcTemplate.update("DELETE FROM quote_number_sequence");

        testCustomerId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO customers (id, code, legal_name, type, rif, alias, credit_days, credit_limit, active, portal_access, created_at, updated_at) " +
                "VALUES (UNHEX(REPLACE(?,'-','')), ?, ?, ?, ?, ?, 0, 0, 1, 0, NOW(), NOW())",
                testCustomerId.toString(),
                System.currentTimeMillis(),
                "Test Customer",
                "JURIDICAL",
                "J-12345678-9",
                "Test Alias"
        );
    }

    private Quote createTestQuote() {
        QuoteLine line = new QuoteLine(
                UUID.randomUUID(),
                "Test Product",
                "PRD-001",
                BigDecimal.ONE,
                new BigDecimal("100.00")
        );

        QuoteAllocation allocation = new QuoteAllocation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Main Warehouse",
                BigDecimal.ONE
        );
        line.addAllocation(allocation);

        return Quote.create(
                QuoteNumber.of(2024, 1),
                testCustomerId,
                "Test Customer",
                "J-12345678-9",
                UUID.randomUUID(),
                "Test User",
                null,
                "Test notes",
                List.of(line)
        );
    }

    @Test
    @DisplayName("should save and retrieve quote with lines and allocations")
    void should_SaveAndRetrieveQuote_WithLinesAndAllocations() {
        Quote quote = createTestQuote();
        Quote saved = repository.save(quote);

        assertNotNull(saved);
        assertEquals(quote.getId(), saved.getId());
        assertEquals(quote.getQuoteNumber(), saved.getQuoteNumber());
        assertEquals(quote.getCustomerName(), saved.getCustomerName());
        assertEquals(quote.getCustomerRif(), saved.getCustomerRif());
        assertEquals(QuoteStatus.DRAFT, saved.getStatus());
        assertEquals(quote.getSubtotal(), saved.getSubtotal());

        assertEquals(1, saved.getLines().size());
        QuoteLine savedLine = saved.getLines().get(0);
        assertEquals("Test Product", savedLine.getProductName());
        assertEquals(BigDecimal.ONE, savedLine.getQuantity());
        assertEquals(new BigDecimal("100.00"), savedLine.getUnitPrice());
        assertEquals(new BigDecimal("100.00"), savedLine.getLineTotal());

        assertEquals(1, savedLine.getAllocations().size());
        QuoteAllocation savedAlloc = savedLine.getAllocations().get(0);
        assertEquals("Main Warehouse", savedAlloc.getWarehouseName());
        assertEquals(BigDecimal.ONE, savedAlloc.getAllocatedQuantity());
    }

    @Test
    @DisplayName("should retrieve saved quote by id")
    void should_RetrieveSavedQuote_ById() {
        Quote quote = createTestQuote();
        repository.save(quote);

        Optional<Quote> found = repository.findById(quote.getId());

        assertTrue(found.isPresent());
        assertEquals(quote.getQuoteNumber(), found.get().getQuoteNumber());
        assertEquals(1, found.get().getLines().size());
    }

    @Test
    @DisplayName("should return empty when finding non-existent quote")
    void should_ReturnEmpty_When_FindingNonExistentQuote() {
        Optional<Quote> found = repository.findById(UUID.randomUUID());
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("should detect existing quote number")
    void should_Detect_ExistingQuoteNumber() {
        Quote quote = createTestQuote();
        repository.save(quote);

        assertTrue(repository.existsByQuoteNumber(quote.getQuoteNumber()));

        assertFalse(repository.existsByQuoteNumber(QuoteNumber.of(2099, 9999)));
    }

    @Test
    @DisplayName("should generate sequential quote numbers for a year")
    void should_GenerateSequentialNumbers_ForYear() {
        int year = 2025;

        int seq1 = repository.getNextSequenceForYear(year);
        int seq2 = repository.getNextSequenceForYear(year);

        assertTrue(seq1 > 0, "First sequence should be > 0");
        assertEquals(seq1 + 1, seq2, "Second call should return next sequential value");
    }

    @Test
    @DisplayName("should delete quote by id")
    void should_DeleteQuote_ById() {
        Quote quote = createTestQuote();
        repository.save(quote);

        repository.delete(quote.getId());

        Optional<Quote> found = repository.findById(quote.getId());
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("should return all saved quotes")
    void should_ReturnAll_SavedQuotes() {
        Quote quote1 = createTestQuote();
        Quote quote2 = Quote.create(
                QuoteNumber.of(2024, 2),
                testCustomerId,
                "Another Customer",
                "V-98765432-1",
                UUID.randomUUID(),
                "Another User",
                null,
                "More notes",
                List.of(new QuoteLine(
                        UUID.randomUUID(),
                        "Another Product",
                        "PRD-002",
                        BigDecimal.TWO,
                        new BigDecimal("50.00")
                ))
        );

        repository.save(quote1);
        repository.save(quote2);

        List<Quote> all = repository.findAll();

        assertTrue(all.size() >= 2);
    }

    @Test
    @DisplayName("should handle multiple lines and allocations in a quote")
    void should_HandleMultipleLinesAndAllocations() {
        QuoteLine line1 = new QuoteLine(
                UUID.randomUUID(), "Product A", "A-001",
                BigDecimal.ONE, new BigDecimal("100.00")
        );
        line1.addAllocation(new QuoteAllocation(
                UUID.randomUUID(), UUID.randomUUID(), "WH-1", BigDecimal.ONE
        ));

        QuoteLine line2 = new QuoteLine(
                UUID.randomUUID(), "Product B", "B-001",
                new BigDecimal("2.0000"), new BigDecimal("75.50")
        );
        line2.addAllocation(new QuoteAllocation(
                UUID.randomUUID(), UUID.randomUUID(), "WH-2", new BigDecimal("1.5000")
        ));
        line2.addAllocation(new QuoteAllocation(
                UUID.randomUUID(), UUID.randomUUID(), "WH-3", new BigDecimal("0.5000")
        ));

        Quote quote = Quote.create(
                QuoteNumber.of(2024, 3),
                testCustomerId, "Multi Customer", "E-11111111-1",
                UUID.randomUUID(), "Multi User",
                null, "Multi line test",
                List.of(line1, line2)
        );

        Quote saved = repository.save(quote);
        Optional<Quote> retrieved = repository.findById(saved.getId());

        assertTrue(retrieved.isPresent());
        assertEquals(2, retrieved.get().getLines().size());

        QuoteLine retrievedLine2 = retrieved.get().getLines().stream()
                .filter(l -> "Product B".equals(l.getProductName()))
                .findFirst()
                .orElseThrow();
        assertEquals("Product B", retrievedLine2.getProductName());
        assertEquals(2, retrievedLine2.getAllocations().size());
    }
}
