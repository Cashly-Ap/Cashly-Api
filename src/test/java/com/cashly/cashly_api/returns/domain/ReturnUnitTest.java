package com.cashly.cashly_api.returns.domain;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Return Domain Entity Unit Tests")
class ReturnUnitTest {

    private static final ReturnNumber RETURN_NUMBER = ReturnNumber.of(2026, 1);
    private static final UUID INVOICE_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final ReturnType RETURN_TYPE = ReturnType.AUTOMATIC;
    private static final String REASON = "Producto defectuoso";
    private static final UUID CREATED_BY = UUID.randomUUID();
    private static final String CREATED_BY_NAME = "Test User";

    private List<ReturnLine> validLines;

    @BeforeEach
    void setUp() {
        validLines = List.of(
                ReturnLine.create(
                        UUID.randomUUID(), UUID.randomUUID(),
                        BigDecimal.ONE, new BigDecimal("50.00"), new BigDecimal("50.00"),
                        List.of()
                )
        );
    }

    @Test
    void should_CreateReturn_When_ValidData() {
        Return return_ = Return.create(
                RETURN_NUMBER, INVOICE_ID, CUSTOMER_ID, RETURN_TYPE, REASON,
                new BigDecimal("50.00"), validLines, CREATED_BY, CREATED_BY_NAME
        );

        assertNotNull(return_.getId());
        assertEquals(RETURN_NUMBER, return_.getReturnNumber());
        assertEquals(INVOICE_ID, return_.getInvoiceId());
        assertEquals(CUSTOMER_ID, return_.getCustomerId());
        assertEquals(RETURN_TYPE, return_.getReturnType());
        assertEquals(REASON, return_.getReason());
        assertEquals(new BigDecimal("50.00"), return_.getTotal());
        assertEquals(1, return_.getLines().size());
        assertEquals(CREATED_BY, return_.getCreatedBy());
        assertEquals(CREATED_BY_NAME, return_.getCreatedByName());
        assertNotNull(return_.getCreatedAt());
        assertNotNull(return_.getUpdatedAt());
    }

    @Test
    void should_CreateReturn_When_MultipleLines() {
        List<ReturnLine> lines = List.of(
                ReturnLine.create(UUID.randomUUID(), UUID.randomUUID(),
                        BigDecimal.ONE, new BigDecimal("30.00"), new BigDecimal("30.00"), List.of()),
                ReturnLine.create(UUID.randomUUID(), UUID.randomUUID(),
                        BigDecimal.valueOf(2), new BigDecimal("10.00"), new BigDecimal("20.00"), List.of())
        );

        Return return_ = Return.create(
                RETURN_NUMBER, INVOICE_ID, CUSTOMER_ID, RETURN_TYPE, REASON,
                new BigDecimal("50.00"), lines, CREATED_BY, CREATED_BY_NAME
        );

        assertEquals(new BigDecimal("50.00"), return_.getTotal());
        assertEquals(2, return_.getLines().size());
    }

    @Test
    void should_ThrowException_When_ReturnNumberIsNull() {
        assertThrows(NullPointerException.class, () ->
                Return.create(null, INVOICE_ID, CUSTOMER_ID, RETURN_TYPE, REASON,
                        new BigDecimal("50.00"), validLines, CREATED_BY, CREATED_BY_NAME)
        );
    }

    @Test
    void should_ThrowException_When_InvoiceIdIsNull() {
        assertThrows(NullPointerException.class, () ->
                Return.create(RETURN_NUMBER, null, CUSTOMER_ID, RETURN_TYPE, REASON,
                        new BigDecimal("50.00"), validLines, CREATED_BY, CREATED_BY_NAME)
        );
    }

    @Test
    void should_ThrowException_When_CustomerIdIsNull() {
        assertThrows(NullPointerException.class, () ->
                Return.create(RETURN_NUMBER, INVOICE_ID, null, RETURN_TYPE, REASON,
                        new BigDecimal("50.00"), validLines, CREATED_BY, CREATED_BY_NAME)
        );
    }

    @Test
    void should_ThrowException_When_ReturnTypeIsNull() {
        assertThrows(NullPointerException.class, () ->
                Return.create(RETURN_NUMBER, INVOICE_ID, CUSTOMER_ID, null, REASON,
                        new BigDecimal("50.00"), validLines, CREATED_BY, CREATED_BY_NAME)
        );
    }

    @Test
    void should_ThrowException_When_ReasonIsNull() {
        assertThrows(NullPointerException.class, () ->
                Return.create(RETURN_NUMBER, INVOICE_ID, CUSTOMER_ID, RETURN_TYPE, null,
                        new BigDecimal("50.00"), validLines, CREATED_BY, CREATED_BY_NAME)
        );
    }

    @Test
    void should_ThrowException_When_ReasonIsBlank() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Return.create(RETURN_NUMBER, INVOICE_ID, CUSTOMER_ID, RETURN_TYPE, "  ",
                        new BigDecimal("50.00"), validLines, CREATED_BY, CREATED_BY_NAME)
        );

        assertEquals("Reason cannot be blank", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_TotalIsNull() {
        assertThrows(NullPointerException.class, () ->
                Return.create(RETURN_NUMBER, INVOICE_ID, CUSTOMER_ID, RETURN_TYPE, REASON,
                        null, validLines, CREATED_BY, CREATED_BY_NAME)
        );
    }

    @Test
    void should_ThrowException_When_CreatedByIsNull() {
        assertThrows(NullPointerException.class, () ->
                Return.create(RETURN_NUMBER, INVOICE_ID, CUSTOMER_ID, RETURN_TYPE, REASON,
                        new BigDecimal("50.00"), validLines, null, CREATED_BY_NAME)
        );
    }

    @Test
    void should_ThrowException_When_CreatedByNameIsNull() {
        assertThrows(NullPointerException.class, () ->
                Return.create(RETURN_NUMBER, INVOICE_ID, CUSTOMER_ID, RETURN_TYPE, REASON,
                        new BigDecimal("50.00"), validLines, CREATED_BY, null)
        );
    }

    @Test
    void should_ThrowException_When_LinesIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Return.create(RETURN_NUMBER, INVOICE_ID, CUSTOMER_ID, RETURN_TYPE, REASON,
                        new BigDecimal("50.00"), null, CREATED_BY, CREATED_BY_NAME)
        );

        assertEquals("Return must have at least one line", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_LinesIsEmpty() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Return.create(RETURN_NUMBER, INVOICE_ID, CUSTOMER_ID, RETURN_TYPE, REASON,
                        new BigDecimal("50.00"), List.of(), CREATED_BY, CREATED_BY_NAME)
        );

        assertEquals("Return must have at least one line", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_TotalDoesNotMatchLineSubtotals() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Return.create(RETURN_NUMBER, INVOICE_ID, CUSTOMER_ID, RETURN_TYPE, REASON,
                        new BigDecimal("99.99"), validLines, CREATED_BY, CREATED_BY_NAME)
        );

        assertEquals("Total does not match sum of line subtotals", exception.getMessage());
    }

    @Test
    void should_Reconstitute_When_AllFieldsProvided() {
        ReturnId id = ReturnId.generate();
        LocalDateTime now = LocalDateTime.now();

        Return return_ = Return.reconstitute(
                id, RETURN_NUMBER, INVOICE_ID, CUSTOMER_ID, RETURN_TYPE, REASON,
                new BigDecimal("50.00"), validLines, List.of(), CREATED_BY, CREATED_BY_NAME, now, now
        );

        assertEquals(id, return_.getId());
        assertEquals(RETURN_NUMBER, return_.getReturnNumber());
        assertEquals(INVOICE_ID, return_.getInvoiceId());
        assertEquals(CUSTOMER_ID, return_.getCustomerId());
        assertEquals(RETURN_TYPE, return_.getReturnType());
        assertEquals(REASON, return_.getReason());
        assertEquals(new BigDecimal("50.00"), return_.getTotal());
        assertEquals(1, return_.getLines().size());
        assertEquals(CREATED_BY, return_.getCreatedBy());
        assertEquals(CREATED_BY_NAME, return_.getCreatedByName());
        assertEquals(now, return_.getCreatedAt());
        assertEquals(now, return_.getUpdatedAt());
    }

    @Test
    void should_ReturnUnmodifiableLines_When_GetLinesCalled() {
        Return return_ = Return.create(
                RETURN_NUMBER, INVOICE_ID, CUSTOMER_ID, RETURN_TYPE, REASON,
                new BigDecimal("50.00"), validLines, CREATED_BY, CREATED_BY_NAME
        );

        assertThrows(UnsupportedOperationException.class, () ->
                return_.getLines().add(validLines.get(0))
        );
    }

    @Test
    void should_BeEqual_When_SameId() {
        Return return1 = Return.create(
                RETURN_NUMBER, INVOICE_ID, CUSTOMER_ID, RETURN_TYPE, REASON,
                new BigDecimal("50.00"), validLines, CREATED_BY, CREATED_BY_NAME
        );
        ReturnId id = return1.getId();

        Return return2 = Return.reconstitute(
                id, RETURN_NUMBER, INVOICE_ID, CUSTOMER_ID, RETURN_TYPE, REASON,
                new BigDecimal("50.00"), validLines, List.of(), CREATED_BY, CREATED_BY_NAME,
                return1.getCreatedAt(), LocalDateTime.now()
        );

        assertEquals(return1, return2);
        assertEquals(return1.hashCode(), return2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentIds() {
        Return return1 = Return.create(
                RETURN_NUMBER, INVOICE_ID, CUSTOMER_ID, RETURN_TYPE, REASON,
                new BigDecimal("50.00"), validLines, CREATED_BY, CREATED_BY_NAME
        );
        Return return2 = Return.create(
                RETURN_NUMBER, INVOICE_ID, CUSTOMER_ID, RETURN_TYPE, REASON,
                new BigDecimal("50.00"), validLines, CREATED_BY, CREATED_BY_NAME
        );

        assertNotEquals(return1, return2);
    }

    @Test
    void should_ReturnString_When_ToStringCalled() {
        Return return_ = Return.create(
                RETURN_NUMBER, INVOICE_ID, CUSTOMER_ID, RETURN_TYPE, REASON,
                new BigDecimal("50.00"), validLines, CREATED_BY, CREATED_BY_NAME
        );

        String result = return_.toString();

        assertTrue(result.contains(return_.getId().toString()));
        assertTrue(result.contains(return_.getReturnNumber().value()));
    }
}
