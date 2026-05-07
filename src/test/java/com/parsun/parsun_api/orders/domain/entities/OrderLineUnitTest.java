package com.parsun.parsun_api.orders.domain.entities;

import com.parsun.parsun_api.orders.domain.valueobjects.OrderLineId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderLine Domain Entity Unit Tests")
class OrderLineUnitTest {

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final String PRODUCT_NAME = "Producto A";
    private static final String PRODUCT_CODE = "PROD-001";
    private static final BigDecimal QUANTITY = new BigDecimal("2");
    private static final BigDecimal UNIT_PRICE = new BigDecimal("10.00");

    @Test
    void should_CreateOrderLine_When_ValidDataProvided() {
        OrderLine line = OrderLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, QUANTITY, UNIT_PRICE, List.of());

        assertNotNull(line.getId());
        assertEquals(PRODUCT_ID, line.getProductId());
        assertEquals(PRODUCT_NAME, line.getProductName());
        assertEquals(PRODUCT_CODE, line.getProductCode());
        assertEquals(QUANTITY, line.getQuantity());
        assertEquals(UNIT_PRICE, line.getUnitPrice());
        assertEquals(new BigDecimal("20.00"), line.getLineTotal());
    }

    @Test
    void should_CalculateLineTotal_When_Created() {
        OrderLine line = OrderLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                new BigDecimal("3"), new BigDecimal("15.50"), List.of());

        assertEquals(new BigDecimal("46.50"), line.getLineTotal());
    }

    @Test
    void should_AllowZeroUnitPrice_When_Created() {
        assertDoesNotThrow(() ->
                OrderLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, QUANTITY, BigDecimal.ZERO, List.of()));
    }

    @Test
    void should_ThrowException_When_ProductIdIsNull() {
        assertThrows(NullPointerException.class, () ->
                OrderLine.create(null, PRODUCT_NAME, PRODUCT_CODE, QUANTITY, UNIT_PRICE, List.of()));
    }

    @Test
    void should_ThrowException_When_ProductNameIsNull() {
        assertThrows(NullPointerException.class, () ->
                OrderLine.create(PRODUCT_ID, null, PRODUCT_CODE, QUANTITY, UNIT_PRICE, List.of()));
    }

    @Test
    void should_ThrowException_When_ProductCodeIsNull() {
        assertThrows(NullPointerException.class, () ->
                OrderLine.create(PRODUCT_ID, PRODUCT_NAME, null, QUANTITY, UNIT_PRICE, List.of()));
    }

    @Test
    void should_ThrowException_When_QuantityIsNull() {
        assertThrows(NullPointerException.class, () ->
                OrderLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, null, UNIT_PRICE, List.of()));
    }

    @Test
    void should_ThrowException_When_UnitPriceIsNull() {
        assertThrows(NullPointerException.class, () ->
                OrderLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, QUANTITY, null, List.of()));
    }

    @Test
    void should_ThrowException_When_QuantityIsZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                OrderLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, BigDecimal.ZERO, UNIT_PRICE, List.of()));

        assertEquals("Quantity must be greater than zero", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_QuantityIsNegative() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                OrderLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                        new BigDecimal("-1"), UNIT_PRICE, List.of()));

        assertEquals("Quantity must be greater than zero", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_UnitPriceIsNegative() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                OrderLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                        QUANTITY, new BigDecimal("-0.01"), List.of()));

        assertEquals("Unit price must be greater than or equal to zero", exception.getMessage());
    }

    @Test
    void should_Reconstitute_When_AllFieldsProvided() {
        OrderLineId id = OrderLineId.generate();

        OrderLine line = OrderLine.reconstitute(id, PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                QUANTITY, UNIT_PRICE, List.of());

        assertEquals(id, line.getId());
        assertEquals(PRODUCT_ID, line.getProductId());
        assertEquals(new BigDecimal("20.00"), line.getLineTotal());
    }

    @Test
    void should_GenerateUniqueId_When_TwoLinesCreated() {
        OrderLine first = OrderLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, QUANTITY, UNIT_PRICE, List.of());
        OrderLine second = OrderLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, QUANTITY, UNIT_PRICE, List.of());

        assertNotEquals(first.getId(), second.getId());
    }
}
