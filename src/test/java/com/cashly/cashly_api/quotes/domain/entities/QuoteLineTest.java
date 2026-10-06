package com.cashly.cashly_api.quotes.domain.entities;

import com.cashly.cashly_api.quotes.domain.exceptions.InvalidQuoteLineException;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteAllocationId;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteLineId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QuoteLine Domain Entity Unit Tests")
class QuoteLineTest {

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final String PRODUCT_NAME = "Product A";
    private static final String PRODUCT_CODE = "PROD-A";
    private static final BigDecimal QUANTITY = new BigDecimal("5");
    private static final BigDecimal UNIT_PRICE = new BigDecimal("25.00");

    @Test
    void should_create_valid_line() {
        QuoteLine line = new QuoteLine(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, QUANTITY, UNIT_PRICE);

        assertNotNull(line.getId());
        assertEquals(PRODUCT_ID, line.getProductId());
        assertEquals(PRODUCT_NAME, line.getProductName());
        assertEquals(PRODUCT_CODE, line.getProductCode());
        assertEquals(QUANTITY, line.getQuantity());
        assertEquals(UNIT_PRICE, line.getUnitPrice());
        assertTrue(line.getAllocations().isEmpty());
    }

    @Test
    void should_calculate_line_total_on_creation() {
        QuoteLine line = new QuoteLine(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                new BigDecimal("3"), new BigDecimal("15.50"));

        assertEquals(new BigDecimal("46.50"), line.getLineTotal());
    }

    @Test
    void should_throw_when_quantity_zero() {
        InvalidQuoteLineException exception = assertThrows(InvalidQuoteLineException.class, () ->
                new QuoteLine(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, BigDecimal.ZERO, UNIT_PRICE));

        assertTrue(exception.getMessage().contains("Quantity must be greater than zero"));
    }

    @Test
    void should_throw_when_quantity_negative() {
        InvalidQuoteLineException exception = assertThrows(InvalidQuoteLineException.class, () ->
                new QuoteLine(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, new BigDecimal("-1"), UNIT_PRICE));

        assertTrue(exception.getMessage().contains("Quantity must be greater than zero"));
    }

    @Test
    void should_throw_when_unit_price_zero() {
        InvalidQuoteLineException exception = assertThrows(InvalidQuoteLineException.class, () ->
                new QuoteLine(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, QUANTITY, BigDecimal.ZERO));

        assertTrue(exception.getMessage().contains("Unit price must be greater than zero"));
    }

    @Test
    void should_throw_when_unit_price_negative() {
        InvalidQuoteLineException exception = assertThrows(InvalidQuoteLineException.class, () ->
                new QuoteLine(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, QUANTITY, new BigDecimal("-0.01")));

        assertTrue(exception.getMessage().contains("Unit price must be greater than zero"));
    }

    @Test
    void should_throw_when_null_product_name() {
        assertThrows(InvalidQuoteLineException.class, () ->
                new QuoteLine(PRODUCT_ID, null, PRODUCT_CODE, QUANTITY, UNIT_PRICE));
    }

    @Test
    void should_throw_when_null_product_code() {
        assertThrows(InvalidQuoteLineException.class, () ->
                new QuoteLine(PRODUCT_ID, PRODUCT_NAME, null, QUANTITY, UNIT_PRICE));
    }

    @Test
    void should_update_quantity_and_recalculate() {
        QuoteLine line = new QuoteLine(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, QUANTITY, UNIT_PRICE);

        line.updateQuantity(new BigDecimal("10"));

        assertEquals(new BigDecimal("10"), line.getQuantity());
        assertEquals(new BigDecimal("250.00"), line.getLineTotal());
    }

    @Test
    void should_update_unit_price_and_recalculate() {
        QuoteLine line = new QuoteLine(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, QUANTITY, UNIT_PRICE);

        line.updateUnitPrice(new BigDecimal("30.00"));

        assertEquals(new BigDecimal("30.00"), line.getUnitPrice());
        assertEquals(new BigDecimal("150.00"), line.getLineTotal());
    }

    @Test
    void should_add_allocation() {
        QuoteLine line = new QuoteLine(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, QUANTITY, UNIT_PRICE);
        QuoteAllocation allocation = new QuoteAllocation(PRODUCT_ID, UUID.randomUUID(), "Warehouse B", new BigDecimal("5"));

        line.addAllocation(allocation);

        assertEquals(1, line.getAllocations().size());
        assertSame(allocation, line.getAllocations().get(0));
    }

    @Test
    void should_remove_allocation() {
        QuoteLine line = new QuoteLine(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, QUANTITY, UNIT_PRICE);
        QuoteAllocation allocation = new QuoteAllocation(PRODUCT_ID, UUID.randomUUID(), "Warehouse B", new BigDecimal("5"));
        line.addAllocation(allocation);

        line.removeAllocation(allocation.getId());

        assertTrue(line.getAllocations().isEmpty());
    }

    @Test
    void should_return_unmodifiable_allocations_list() {
        QuoteLine line = new QuoteLine(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, QUANTITY, UNIT_PRICE);

        assertThrows(UnsupportedOperationException.class, () ->
                line.getAllocations().add(
                        new QuoteAllocation(PRODUCT_ID, UUID.randomUUID(), "Warehouse B", new BigDecimal("5"))));
    }

    @Test
    void should_reconstitute_without_validation() {
        QuoteLineId id = QuoteLineId.generate();
        QuoteAllocationId allocId = QuoteAllocationId.generate();
        QuoteAllocation allocation = QuoteAllocation.reconstitute(allocId, PRODUCT_ID, UUID.randomUUID(), "Warehouse B", BigDecimal.ZERO);
        List<QuoteAllocation> allocations = List.of(allocation);

        QuoteLine line = QuoteLine.reconstitute(id, PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, allocations);

        assertEquals(id, line.getId());
        assertEquals(BigDecimal.ZERO, line.getQuantity());
        assertEquals(BigDecimal.ZERO, line.getUnitPrice());
        assertEquals(BigDecimal.ZERO, line.getLineTotal());
        assertEquals(1, line.getAllocations().size());
    }

    @Test
    void should_generate_unique_id_per_line() {
        QuoteLine first = new QuoteLine(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, QUANTITY, UNIT_PRICE);
        QuoteLine second = new QuoteLine(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, QUANTITY, UNIT_PRICE);

        assertNotEquals(first.getId(), second.getId());
    }
}
