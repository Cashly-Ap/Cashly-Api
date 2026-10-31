package com.cashly.cashly_api.sales.quotes.domain.entity;

import com.cashly.cashly_api.sales.quotes.domain.exceptions.InvalidQuoteAllocationException;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteAllocationId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QuoteAllocation Domain Entity Unit Tests")
class QuoteAllocationTest {

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID WAREHOUSE_ID = UUID.randomUUID();
    private static final String WAREHOUSE_NAME = "Warehouse A";
    private static final BigDecimal ALLOCATED_QUANTITY = new BigDecimal("10");

    @Test
    void should_create_valid_allocation() {
        QuoteAllocation allocation = new QuoteAllocation(PRODUCT_ID,
                WAREHOUSE_ID, WAREHOUSE_NAME, ALLOCATED_QUANTITY);

        assertNotNull(allocation.getId());
        assertEquals(PRODUCT_ID, allocation.getProductId());
        assertEquals(WAREHOUSE_ID, allocation.getWarehouseId());
        assertEquals(WAREHOUSE_NAME, allocation.getWarehouseName());
        assertEquals(ALLOCATED_QUANTITY, allocation.getAllocatedQuantity());
    }

    @Test
    void should_throw_when_allocated_quantity_zero() {
        InvalidQuoteAllocationException exception = assertThrows(
                InvalidQuoteAllocationException.class,
                () -> new QuoteAllocation(PRODUCT_ID, WAREHOUSE_ID,
                        WAREHOUSE_NAME, BigDecimal.ZERO));

        assertTrue(exception.getMessage()
                .contains("Allocated quantity must be greater than zero"));
    }

    @Test
    void should_throw_when_allocated_quantity_negative() {
        InvalidQuoteAllocationException exception = assertThrows(
                InvalidQuoteAllocationException.class,
                () -> new QuoteAllocation(PRODUCT_ID, WAREHOUSE_ID,
                        WAREHOUSE_NAME, new BigDecimal("-5")));

        assertTrue(exception.getMessage()
                .contains("Allocated quantity must be greater than zero"));
    }

    @Test
    void should_throw_when_null_product_id() {
        assertThrows(InvalidQuoteAllocationException.class,
                () -> new QuoteAllocation(null, WAREHOUSE_ID, WAREHOUSE_NAME,
                        ALLOCATED_QUANTITY));
    }

    @Test
    void should_throw_when_null_warehouse_id() {
        assertThrows(InvalidQuoteAllocationException.class,
                () -> new QuoteAllocation(PRODUCT_ID, null, WAREHOUSE_NAME,
                        ALLOCATED_QUANTITY));
    }

    @Test
    void should_throw_when_null_warehouse_name() {
        assertThrows(InvalidQuoteAllocationException.class,
                () -> new QuoteAllocation(PRODUCT_ID, WAREHOUSE_ID, null,
                        ALLOCATED_QUANTITY));
    }

    @Test
    void should_update_allocated_quantity() {
        QuoteAllocation allocation = new QuoteAllocation(PRODUCT_ID,
                WAREHOUSE_ID, WAREHOUSE_NAME, ALLOCATED_QUANTITY);

        allocation.updateAllocatedQuantity(new BigDecimal("20"));

        assertEquals(new BigDecimal("20"), allocation.getAllocatedQuantity());
    }

    @Test
    void should_throw_when_update_to_zero() {
        QuoteAllocation allocation = new QuoteAllocation(PRODUCT_ID,
                WAREHOUSE_ID, WAREHOUSE_NAME, ALLOCATED_QUANTITY);

        InvalidQuoteAllocationException exception = assertThrows(
                InvalidQuoteAllocationException.class,
                () -> allocation.updateAllocatedQuantity(BigDecimal.ZERO));

        assertTrue(exception.getMessage()
                .contains("Allocated quantity must be greater than zero"));
    }

    @Test
    void should_throw_when_update_to_negative() {
        QuoteAllocation allocation = new QuoteAllocation(PRODUCT_ID,
                WAREHOUSE_ID, WAREHOUSE_NAME, ALLOCATED_QUANTITY);

        InvalidQuoteAllocationException exception = assertThrows(
                InvalidQuoteAllocationException.class,
                () -> allocation.updateAllocatedQuantity(new BigDecimal("-1")));

        assertTrue(exception.getMessage()
                .contains("Allocated quantity must be greater than zero"));
    }

    @Test
    void should_reconstitute_without_validation() {
        QuoteAllocationId id = QuoteAllocationId.generate();

        QuoteAllocation allocation = QuoteAllocation.reconstitute(id,
                PRODUCT_ID, WAREHOUSE_ID, WAREHOUSE_NAME, BigDecimal.ZERO);

        assertEquals(id, allocation.getId());
        assertEquals(PRODUCT_ID, allocation.getProductId());
        assertEquals(WAREHOUSE_ID, allocation.getWarehouseId());
        assertEquals(WAREHOUSE_NAME, allocation.getWarehouseName());
        assertEquals(BigDecimal.ZERO, allocation.getAllocatedQuantity());
    }
}
