package com.parsun.parsun_api.orders.domain.entities;

import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderNumber;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order Domain Entity Unit Tests")
class OrderUnitTest {

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID CREATED_BY = UUID.randomUUID();
    private static final String CUSTOMER_NAME = "Cliente Test";
    private static final String CUSTOMER_RIF = "J-12345678-9";
    private static final String CREATED_BY_NAME = "Vendedor Test";
    private static final OrderNumber ORDER_NUMBER = OrderNumber.of(2026, 1);

    private List<OrderLine> validLines;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        futureDate = LocalDate.now().plusDays(1);
        validLines = List.of(
                OrderLine.create(UUID.randomUUID(), "Producto A", "PROD-001",
                        new BigDecimal("2"), new BigDecimal("10.00"))
        );
    }

    @Test
    void should_CreateOrder_When_ValidDataProvided() {
        Order order = Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, futureDate, "Notas", validLines);

        assertNotNull(order.getId());
        assertEquals(ORDER_NUMBER, order.getOrderNumber());
        assertEquals(CUSTOMER_ID, order.getCustomerId());
        assertEquals(CUSTOMER_NAME, order.getCustomerName());
        assertEquals(CUSTOMER_RIF, order.getCustomerRif());
        assertEquals(CREATED_BY, order.getCreatedBy());
        assertEquals(CREATED_BY_NAME, order.getCreatedByName());
        assertEquals(OrderStatus.DRAFT, order.getStatus());
        assertEquals(futureDate, order.getDeliveryDate());
        assertEquals("Notas", order.getNotes());
        assertEquals(new BigDecimal("20.00"), order.getSubtotal());
        assertNull(order.getInvoiceId());
        assertNull(order.getInvoiceNumber());
        assertNull(order.getCancelledAt());
        assertNull(order.getCancelledReason());
        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
        assertEquals(1, order.getLines().size());
    }

    @Test
    void should_CreateOrder_When_DeliveryDateIsToday() {
        assertDoesNotThrow(() ->
                Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                        CREATED_BY, CREATED_BY_NAME, LocalDate.now(), null, validLines));
    }

    @Test
    void should_AllowNullNotes_When_Creating() {
        Order order = Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, futureDate, null, validLines);

        assertNull(order.getNotes());
    }

    @Test
    void should_CalculateSubtotal_When_MultipleLines() {
        List<OrderLine> lines = List.of(
                OrderLine.create(UUID.randomUUID(), "Producto A", "PROD-001",
                        new BigDecimal("2"), new BigDecimal("10.00")),
                OrderLine.create(UUID.randomUUID(), "Producto B", "PROD-002",
                        new BigDecimal("3"), new BigDecimal("5.00"))
        );

        Order order = Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, futureDate, null, lines);

        assertEquals(new BigDecimal("35.00"), order.getSubtotal());
    }

    @Test
    void should_ThrowException_When_OrderNumberIsNull() {
        assertThrows(NullPointerException.class, () ->
                Order.create(null, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                        CREATED_BY, CREATED_BY_NAME, futureDate, null, validLines));
    }

    @Test
    void should_ThrowException_When_CustomerIdIsNull() {
        assertThrows(NullPointerException.class, () ->
                Order.create(ORDER_NUMBER, null, CUSTOMER_NAME, CUSTOMER_RIF,
                        CREATED_BY, CREATED_BY_NAME, futureDate, null, validLines));
    }

    @Test
    void should_ThrowException_When_CustomerNameIsNull() {
        assertThrows(NullPointerException.class, () ->
                Order.create(ORDER_NUMBER, CUSTOMER_ID, null, CUSTOMER_RIF,
                        CREATED_BY, CREATED_BY_NAME, futureDate, null, validLines));
    }

    @Test
    void should_ThrowException_When_CustomerRifIsNull() {
        assertThrows(NullPointerException.class, () ->
                Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, null,
                        CREATED_BY, CREATED_BY_NAME, futureDate, null, validLines));
    }

    @Test
    void should_ThrowException_When_CreatedByIsNull() {
        assertThrows(NullPointerException.class, () ->
                Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                        null, CREATED_BY_NAME, futureDate, null, validLines));
    }

    @Test
    void should_ThrowException_When_CreatedByNameIsNull() {
        assertThrows(NullPointerException.class, () ->
                Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                        CREATED_BY, null, futureDate, null, validLines));
    }

    @Test
    void should_ThrowException_When_DeliveryDateIsNull() {
        assertThrows(NullPointerException.class, () ->
                Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                        CREATED_BY, CREATED_BY_NAME, null, null, validLines));
    }

    @Test
    void should_ThrowException_When_DeliveryDateIsInThePast() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                        CREATED_BY, CREATED_BY_NAME, yesterday, null, validLines));

        assertEquals("Delivery date must be today or in the future", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_LinesIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                        CREATED_BY, CREATED_BY_NAME, futureDate, null, null));

        assertEquals("Order must have at least one line", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_LinesIsEmpty() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                        CREATED_BY, CREATED_BY_NAME, futureDate, null, List.of()));

        assertEquals("Order must have at least one line", exception.getMessage());
    }

    @Test
    void should_ReturnUnmodifiableLines_When_GetLinesCalled() {
        Order order = Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, futureDate, null, validLines);

        assertThrows(UnsupportedOperationException.class, () ->
                order.getLines().add(validLines.get(0)));
    }

    // --- update ---

    @Test
    void should_UpdateOrder_When_StatusIsPending() throws InterruptedException {
        Order order = Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, futureDate, "original", validLines);
        LocalDateTime beforeUpdate = order.getUpdatedAt();

        List<OrderLine> newLines = List.of(
                OrderLine.create(UUID.randomUUID(), "Producto B", "PROD-002",
                        new BigDecimal("5"), new BigDecimal("3.00"))
        );
        LocalDate newDate = futureDate.plusDays(1);

        Thread.sleep(10);
        order.update(newDate, "actualizado", newLines);

        assertEquals(newDate, order.getDeliveryDate());
        assertEquals("actualizado", order.getNotes());
        assertEquals(1, order.getLines().size());
        assertEquals(new BigDecimal("15.00"), order.getSubtotal());
        assertTrue(order.getUpdatedAt().isAfter(beforeUpdate));
    }

    @Test
    void should_ThrowException_When_UpdatingNonPendingOrder() {
        Order order = Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, futureDate, null, validLines);
        order.cancel("Motivo válido");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                order.update(futureDate, null, validLines));

        assertEquals("Only DRAFT orders can be updated", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_UpdatingWithPastDeliveryDate() {
        Order order = Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, futureDate, null, validLines);

        assertThrows(IllegalArgumentException.class, () ->
                order.update(LocalDate.now().minusDays(1), null, validLines));
    }

    @Test
    void should_ThrowException_When_UpdatingWithEmptyLines() {
        Order order = Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, futureDate, null, validLines);

        assertThrows(IllegalArgumentException.class, () ->
                order.update(futureDate, null, List.of()));
    }

    // --- cancel ---

    @Test
    void should_CancelOrder_When_StatusIsPending() throws InterruptedException {
        Order order = Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, futureDate, null, validLines);
        LocalDateTime beforeCancel = order.getUpdatedAt();

        Thread.sleep(10);
        order.cancel("Cliente solicitó cancelación");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertNotNull(order.getCancelledAt());
        assertEquals("Cliente solicitó cancelación", order.getCancelledReason());
        assertTrue(order.getUpdatedAt().isAfter(beforeCancel));
    }

    @Test
    void should_ThrowException_When_CancellingNonPendingOrder() {
        Order order = Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, futureDate, null, validLines);
        order.cancel("Primera cancelación");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                order.cancel("Segunda cancelación"));

        assertEquals("Only DRAFT or CONFIRMED orders can be cancelled", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_CancelReasonIsNull() {
        Order order = Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, futureDate, null, validLines);

        assertThrows(NullPointerException.class, () -> order.cancel(null));
    }

    @Test
    void should_ThrowException_When_CancelReasonIsBlank() {
        Order order = Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, futureDate, null, validLines);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                order.cancel("   "));

        assertEquals("Cancellation reason cannot be blank", exception.getMessage());
    }

    // --- markAsInvoiced ---

    @Test
    void should_MarkAsInvoiced_When_StatusIsConfirmed() throws InterruptedException {
        Order order = Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, futureDate, null, validLines);
        order.confirm();
        UUID invoiceId = UUID.randomUUID();
        LocalDateTime beforeInvoice = order.getUpdatedAt();

        Thread.sleep(10);
        order.markAsInvoiced(invoiceId, "FAC-2026-00001");

        assertEquals(OrderStatus.INVOICED, order.getStatus());
        assertEquals(invoiceId, order.getInvoiceId());
        assertEquals("FAC-2026-00001", order.getInvoiceNumber());
        assertTrue(order.getUpdatedAt().isAfter(beforeInvoice));
    }

    @Test
    void should_ThrowException_When_InvoicingNonPendingOrder() {
        Order order = Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, futureDate, null, validLines);
        order.cancel("Cancelado");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                order.markAsInvoiced(UUID.randomUUID(), "FAC-2026-00001"));

        assertEquals("Only CONFIRMED orders can be invoiced", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_InvoiceIdIsNull() {
        Order order = Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, futureDate, null, validLines);
        order.confirm();

        assertThrows(NullPointerException.class, () ->
                order.markAsInvoiced(null, "FAC-2026-00001"));
    }

    @Test
    void should_ThrowException_When_InvoiceNumberIsNull() {
        Order order = Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, futureDate, null, validLines);
        order.confirm();

        assertThrows(NullPointerException.class, () ->
                order.markAsInvoiced(UUID.randomUUID(), null));
    }

    // --- isOwnedBy ---

    @Test
    void should_ReturnTrue_When_UserIsOwner() {
        Order order = Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, futureDate, null, validLines);

        assertTrue(order.isOwnedBy(CREATED_BY));
    }

    @Test
    void should_ReturnFalse_When_UserIsNotOwner() {
        Order order = Order.create(ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, futureDate, null, validLines);

        assertFalse(order.isOwnedBy(UUID.randomUUID()));
    }

    // --- reconstitute ---

    @Test
    void should_Reconstitute_When_AllFieldsProvided() {
        OrderId id = OrderId.generate();
        LocalDateTime now = LocalDateTime.now();
        UUID invoiceId = UUID.randomUUID();

        Order order = Order.reconstitute(id, ORDER_NUMBER, CUSTOMER_ID, CUSTOMER_NAME,
                CUSTOMER_RIF, CREATED_BY, CREATED_BY_NAME, OrderStatus.INVOICED,
                futureDate, "notas", invoiceId, "FAC-2026-00001",
                null, null, validLines, now, now);

        assertEquals(id, order.getId());
        assertEquals(OrderStatus.INVOICED, order.getStatus());
        assertEquals(invoiceId, order.getInvoiceId());
        assertEquals("FAC-2026-00001", order.getInvoiceNumber());
        assertEquals(now, order.getCreatedAt());
    }
}
