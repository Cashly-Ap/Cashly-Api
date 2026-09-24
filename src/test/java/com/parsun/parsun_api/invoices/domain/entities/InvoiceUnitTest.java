package com.parsun.parsun_api.invoices.domain.entities;

import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceLineId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceReturnStatus;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import com.parsun.parsun_api.invoices.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.returns.domain.entities.Return;
import com.parsun.parsun_api.returns.domain.entities.ReturnLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Invoice Domain Entity Unit Tests")
class InvoiceUnitTest {

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID CREATED_BY = UUID.randomUUID();
    private static final String CUSTOMER_NAME = "Cliente Test";
    private static final String CUSTOMER_RIF = "J-12345678-9";
    private static final String CREATED_BY_NAME = "Vendedor Test";
    private static final InvoiceNumber INVOICE_NUMBER = InvoiceNumber.of(2026, 1);

    private List<InvoiceLine> validLines;

    @BeforeEach
    void setUp() {
        validLines = List.of(
                InvoiceLine.create(UUID.randomUUID(), "Producto A", "PROD-001",
                        new BigDecimal("2"), new BigDecimal("10.00"), List.of())
        );
    }

    

    @Test
    void should_CreateInvoice_When_ValidDataProvided() {
        Invoice invoice = Invoice.create(INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, null, null, PaymentType.CASH, "Notas", validLines);

        assertNotNull(invoice.getId());
        assertEquals(INVOICE_NUMBER, invoice.getInvoiceNumber());
        assertEquals(CUSTOMER_ID, invoice.getCustomerId());
        assertEquals(CUSTOMER_NAME, invoice.getCustomerName());
        assertEquals(CUSTOMER_RIF, invoice.getCustomerRif());
        assertEquals(CREATED_BY, invoice.getCreatedBy());
        assertEquals(CREATED_BY_NAME, invoice.getCreatedByName());
        assertEquals(InvoiceStatus.DRAFT, invoice.getStatus());
        assertEquals(PaymentType.CASH, invoice.getPaymentType());
        assertEquals("Notas", invoice.getNotes());
        assertEquals(new BigDecimal("20.00"), invoice.getSubtotal());
        assertNull(invoice.getOrderId());
        assertNull(invoice.getOrderNumber());
        assertNull(invoice.getCancelledAt());
        assertNull(invoice.getCancelledReason());
        assertNotNull(invoice.getCreatedAt());
        assertNotNull(invoice.getUpdatedAt());
        assertEquals(1, invoice.getLines().size());
    }

    @Test
    void should_PreserveConfiguredIssueAndDueDates() {
        LocalDate issueDate = LocalDate.of(2026, 9, 1);
        LocalDate dueDate = issueDate.plusDays(29);

        Invoice invoice = Invoice.create(INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, null, null, List.of(), List.of(), PaymentType.CREDIT,
                null, validLines, issueDate, dueDate);

        assertEquals(issueDate, invoice.getIssueDate());
        assertEquals(LocalDate.of(2026, 9, 30), invoice.getDueDate());
    }

    @Test
    void should_CreateInvoice_When_LinkedToOrder() {
        UUID orderId = UUID.randomUUID();
        String orderNumber = "ORD-2026-00001";

        Invoice invoice = Invoice.create(INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, orderId, orderNumber, PaymentType.CREDIT, null, validLines);

        assertEquals(orderId, invoice.getOrderId());
        assertEquals(orderNumber, invoice.getOrderNumber());
    }

    @Test
    void should_AllowNullNotes_When_Creating() {
        Invoice invoice = Invoice.create(INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, null, null, PaymentType.CASH, null, validLines);

        assertNull(invoice.getNotes());
    }

    @Test
    void should_CalculateSubtotal_When_MultipleLines() {
        List<InvoiceLine> lines = List.of(
                InvoiceLine.create(UUID.randomUUID(), "Producto A", "PROD-001",
                        new BigDecimal("2"), new BigDecimal("10.00"), List.of()),
                InvoiceLine.create(UUID.randomUUID(), "Producto B", "PROD-002",
                        new BigDecimal("3"), new BigDecimal("5.00"), List.of())
        );

        Invoice invoice = Invoice.create(INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, null, null, PaymentType.CASH, null, lines);

        assertEquals(new BigDecimal("35.00"), invoice.getSubtotal());
    }

    @Test
    void should_ThrowException_When_InvoiceNumberIsNull() {
        assertThrows(NullPointerException.class, () ->
                Invoice.create(null, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                        CREATED_BY, CREATED_BY_NAME, null, null, PaymentType.CASH, null, validLines));
    }

    @Test
    void should_ThrowException_When_CustomerIdIsNull() {
        assertThrows(NullPointerException.class, () ->
                Invoice.create(INVOICE_NUMBER, null, CUSTOMER_NAME, CUSTOMER_RIF,
                        CREATED_BY, CREATED_BY_NAME, null, null, PaymentType.CASH, null, validLines));
    }

    @Test
    void should_ThrowException_When_CustomerNameIsNull() {
        assertThrows(NullPointerException.class, () ->
                Invoice.create(INVOICE_NUMBER, CUSTOMER_ID, null, CUSTOMER_RIF,
                        CREATED_BY, CREATED_BY_NAME, null, null, PaymentType.CASH, null, validLines));
    }

    @Test
    void should_ThrowException_When_CustomerRifIsNull() {
        assertThrows(NullPointerException.class, () ->
                Invoice.create(INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, null,
                        CREATED_BY, CREATED_BY_NAME, null, null, PaymentType.CASH, null, validLines));
    }

    @Test
    void should_ThrowException_When_CreatedByIsNull() {
        assertThrows(NullPointerException.class, () ->
                Invoice.create(INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                        null, CREATED_BY_NAME, null, null, PaymentType.CASH, null, validLines));
    }

    @Test
    void should_ThrowException_When_CreatedByNameIsNull() {
        assertThrows(NullPointerException.class, () ->
                Invoice.create(INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                        CREATED_BY, null, null, null, PaymentType.CASH, null, validLines));
    }

    @Test
    void should_ThrowException_When_PaymentTypeIsNull() {
        assertThrows(NullPointerException.class, () ->
                Invoice.create(INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                        CREATED_BY, CREATED_BY_NAME, null, null, null, null, validLines));
    }

    @Test
    void should_ThrowException_When_LinesIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Invoice.create(INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                        CREATED_BY, CREATED_BY_NAME, null, null, PaymentType.CASH, null, null));

        assertEquals("Invoice must have at least one line", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_LinesIsEmpty() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Invoice.create(INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                        CREATED_BY, CREATED_BY_NAME, null, null, PaymentType.CASH, null, List.of()));

        assertEquals("Invoice must have at least one line", exception.getMessage());
    }

    @Test
    void should_ReturnUnmodifiableLines_When_GetLinesCalled() {
        Invoice invoice = Invoice.create(INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, null, null, PaymentType.CASH, null, validLines);

        assertThrows(UnsupportedOperationException.class, () ->
                invoice.getLines().add(validLines.get(0)));
    }

    

    @Test
    void should_TransitionToPaid_When_ConfirmingCashInvoice() throws InterruptedException {
        Invoice invoice = Invoice.create(INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, null, null, PaymentType.CASH, null, validLines);
        LocalDateTime beforeConfirm = invoice.getUpdatedAt();

        Thread.sleep(10);
        invoice.confirm();

        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertTrue(invoice.getUpdatedAt().isAfter(beforeConfirm));
    }

    @Test
    void should_TransitionToPendingPayment_When_ConfirmingCreditInvoice() throws InterruptedException {
        Invoice invoice = Invoice.create(INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, null, null, PaymentType.CREDIT, null, validLines);
        LocalDateTime beforeConfirm = invoice.getUpdatedAt();

        Thread.sleep(10);
        invoice.confirm();

        assertEquals(InvoiceStatus.PENDING_PAYMENT, invoice.getStatus());
        assertTrue(invoice.getUpdatedAt().isAfter(beforeConfirm));
    }

    @Test
    void should_ThrowException_When_ConfirmingNonDraftInvoice() {
        Invoice invoice = Invoice.create(INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, null, null, PaymentType.CASH, null, validLines);
        invoice.cancel("Motivo válido");

        IllegalStateException exception = assertThrows(IllegalStateException.class, invoice::confirm);

        assertEquals("Only DRAFT invoices can be confirmed", exception.getMessage());
    }

    

    @Test
    void should_CancelInvoice_When_StatusIsDraft() throws InterruptedException {
        Invoice invoice = Invoice.create(INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, null, null, PaymentType.CASH, null, validLines);
        LocalDateTime beforeCancel = invoice.getUpdatedAt();

        Thread.sleep(10);
        invoice.cancel("Cliente solicitó cancelación");

        assertEquals(InvoiceStatus.CANCELLED, invoice.getStatus());
        assertNotNull(invoice.getCancelledAt());
        assertEquals("Cliente solicitó cancelación", invoice.getCancelledReason());
        assertTrue(invoice.getUpdatedAt().isAfter(beforeCancel));
    }

    @Test
    void should_ThrowException_When_CancellingAlreadyCancelledInvoice() {
        Invoice invoice = Invoice.create(INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, null, null, PaymentType.CASH, null, validLines);
        invoice.cancel("Primera cancelación");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                invoice.cancel("Segunda cancelación"));

        assertEquals("Invoice is already cancelled", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_CancelReasonIsNull() {
        Invoice invoice = Invoice.create(INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, null, null, PaymentType.CASH, null, validLines);

        assertThrows(NullPointerException.class, () -> invoice.cancel(null));
    }

    @Test
    void should_ThrowException_When_CancelReasonIsBlank() {
        Invoice invoice = Invoice.create(INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, null, null, PaymentType.CASH, null, validLines);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                invoice.cancel("   "));

        assertEquals("Cancellation reason cannot be blank", exception.getMessage());
    }

    

    @Test
    void should_Reconstitute_When_AllFieldsProvided() {
        InvoiceId id = InvoiceId.generate();
        LocalDateTime now = LocalDateTime.now();
        UUID orderId = UUID.randomUUID();

        Invoice invoice = Invoice.reconstitute(id, INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME,
                CUSTOMER_RIF, CREATED_BY, CREATED_BY_NAME, orderId, "ORD-2026-00001",
                InvoiceStatus.PAID, PaymentType.CASH, "notas",
                BigDecimal.ZERO, null, null, null, validLines, now, now);

        assertEquals(id, invoice.getId());
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertEquals(orderId, invoice.getOrderId());
        assertEquals("ORD-2026-00001", invoice.getOrderNumber());
        assertEquals(now, invoice.getCreatedAt());
    }

    

    @Test
    void should_CalculateTotal_When_NormalCase() {
        List<InvoiceLine> lines = List.of(
                InvoiceLine.create(UUID.randomUUID(), "Producto A", "PROD-001",
                        new BigDecimal("2"), new BigDecimal("10.00"), List.of()),
                InvoiceLine.create(UUID.randomUUID(), "Producto B", "PROD-002",
                        new BigDecimal("3"), new BigDecimal("5.00"), List.of())
        );
        Invoice invoice = Invoice.reconstitute(InvoiceId.generate(), INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME,
                CUSTOMER_RIF, CREATED_BY, CREATED_BY_NAME, null, null,
                InvoiceStatus.PAID, PaymentType.CASH, null,
                new BigDecimal("5.00"), null,
                null, null, lines, LocalDateTime.now(), LocalDateTime.now());

        BigDecimal result = invoice.calculateTotal(new BigDecimal("10.00"), new BigDecimal("3.00"));

        
        assertEquals(new BigDecimal("23.00"), result);
    }

    @Test
    void should_CalculateTotal_When_FullyReturned() {
        List<InvoiceLine> lines = List.of(
                InvoiceLine.create(UUID.randomUUID(), "Producto A", "PROD-001",
                        new BigDecimal("2"), new BigDecimal("10.00"), List.of())
        );
        Invoice invoice = Invoice.reconstitute(InvoiceId.generate(), INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME,
                CUSTOMER_RIF, CREATED_BY, CREATED_BY_NAME, null, null,
                InvoiceStatus.FULLY_RETURNED, PaymentType.CASH, null,
                new BigDecimal("20.00"), InvoiceReturnStatus.FULL,
                null, null, lines, LocalDateTime.now(), LocalDateTime.now());

        BigDecimal result = invoice.calculateTotal(BigDecimal.ZERO, BigDecimal.ZERO);

        
        
        assertEquals(0, result.compareTo(BigDecimal.ZERO), "Expected 0.00 to be equal to 0 in value");
    }

    @Test
    void should_CalculateTotal_When_ZeroDefaultOverload() {
        List<InvoiceLine> lines = List.of(
                InvoiceLine.create(UUID.randomUUID(), "Producto A", "PROD-001",
                        new BigDecimal("5"), new BigDecimal("100.00"), List.of())
        );
        Invoice invoice = Invoice.reconstitute(InvoiceId.generate(), INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME,
                CUSTOMER_RIF, CREATED_BY, CREATED_BY_NAME, null, null,
                InvoiceStatus.PAID, PaymentType.CASH, null,
                new BigDecimal("100.00"), null,
                null, null, lines, LocalDateTime.now(), LocalDateTime.now());

        BigDecimal result = invoice.calculateTotal();

        
        assertEquals(new BigDecimal("400.00"), result);
    }

    @Test
    void should_CalculateTotal_When_NullSubtotal() throws Exception {
        Invoice invoice = Invoice.reconstitute(InvoiceId.generate(), INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME,
                CUSTOMER_RIF, CREATED_BY, CREATED_BY_NAME, null, null,
                InvoiceStatus.PAID, PaymentType.CASH, null,
                BigDecimal.ZERO, null,
                null, null, List.of(), LocalDateTime.now(), LocalDateTime.now());

        java.lang.reflect.Field subtotalField = Invoice.class.getDeclaredField("subtotal");
        subtotalField.setAccessible(true);
        subtotalField.set(invoice, null);

        BigDecimal result = invoice.calculateTotal(BigDecimal.TEN, BigDecimal.ONE);

        assertEquals(BigDecimal.ZERO, result);
    }


    // ============================================================
    // revertReturn()
    // ============================================================

    @Test
    void should_RevertReturnedTotalAndReturnedQuantity_When_ValidReturnProvided() {
        InvoiceLine line = InvoiceLine.reconstitute(
                InvoiceLineId.generate(), UUID.randomUUID(), "Product", "CODE",
                new BigDecimal("10"), BigDecimal.TEN, new BigDecimal("5"), List.of());
        InvoiceId invoiceId = InvoiceId.generate();
        Invoice invoice = Invoice.reconstitute(
                invoiceId, INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, null, null,
                InvoiceStatus.PAID, PaymentType.CASH, null,
                new BigDecimal("50.00"), InvoiceReturnStatus.PARTIAL,
                null, null, List.of(line), LocalDateTime.now(), LocalDateTime.now());

        Return return_ = mock(Return.class);
        ReturnLine returnLine = mock(ReturnLine.class);
        when(return_.getInvoiceId()).thenReturn(invoiceId);
        when(return_.getTotal()).thenReturn(new BigDecimal("50.00"));
        when(return_.getLines()).thenReturn(List.of(returnLine));
        when(returnLine.getInvoiceLineId()).thenReturn(line.getId().value());
        when(returnLine.getQuantity()).thenReturn(new BigDecimal("5"));

        invoice.revertReturn(return_);

        assertEquals(0, invoice.getReturnedTotal().compareTo(BigDecimal.ZERO));
        assertEquals(0, line.getReturnedQuantity().compareTo(BigDecimal.ZERO));
    }

    @Test
    void should_SetReturnStatusToNull_When_NoReturnsRemain() {
        InvoiceLine line = InvoiceLine.reconstitute(
                InvoiceLineId.generate(), UUID.randomUUID(), "Product", "CODE",
                new BigDecimal("10"), BigDecimal.TEN, new BigDecimal("5"), List.of());
        InvoiceId invoiceId = InvoiceId.generate();
        Invoice invoice = Invoice.reconstitute(
                invoiceId, INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, null, null,
                InvoiceStatus.PAID, PaymentType.CASH, null,
                new BigDecimal("50.00"), InvoiceReturnStatus.PARTIAL,
                null, null, List.of(line), LocalDateTime.now(), LocalDateTime.now());

        Return return_ = mock(Return.class);
        ReturnLine returnLine = mock(ReturnLine.class);
        when(return_.getInvoiceId()).thenReturn(invoiceId);
        when(return_.getTotal()).thenReturn(new BigDecimal("50.00"));
        when(return_.getLines()).thenReturn(List.of(returnLine));
        when(returnLine.getInvoiceLineId()).thenReturn(line.getId().value());
        when(returnLine.getQuantity()).thenReturn(new BigDecimal("5"));

        invoice.revertReturn(return_);

        assertNull(invoice.getReturnStatus());
    }

    @Test
    void should_SetReturnStatusToPartial_When_SomeReturnsRemain() {
        InvoiceLine line1 = InvoiceLine.reconstitute(
                InvoiceLineId.generate(), UUID.randomUUID(), "Product A", "CODE-A",
                new BigDecimal("10"), BigDecimal.TEN, new BigDecimal("5"), List.of());
        InvoiceLine line2 = InvoiceLine.reconstitute(
                InvoiceLineId.generate(), UUID.randomUUID(), "Product B", "CODE-B",
                new BigDecimal("10"), BigDecimal.TEN, new BigDecimal("3"), List.of());
        InvoiceId invoiceId = InvoiceId.generate();
        Invoice invoice = Invoice.reconstitute(
                invoiceId, INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, null, null,
                InvoiceStatus.PAID, PaymentType.CASH, null,
                new BigDecimal("80.00"), InvoiceReturnStatus.PARTIAL,
                null, null, List.of(line1, line2), LocalDateTime.now(), LocalDateTime.now());

        Return return_ = mock(Return.class);
        ReturnLine returnLine = mock(ReturnLine.class);
        when(return_.getInvoiceId()).thenReturn(invoiceId);
        when(return_.getTotal()).thenReturn(new BigDecimal("20.00"));
        when(return_.getLines()).thenReturn(List.of(returnLine));
        when(returnLine.getInvoiceLineId()).thenReturn(line1.getId().value());
        when(returnLine.getQuantity()).thenReturn(new BigDecimal("2"));

        invoice.revertReturn(return_);

        assertEquals(InvoiceReturnStatus.PARTIAL, invoice.getReturnStatus());
        assertEquals(new BigDecimal("60.00"), invoice.getReturnedTotal());
    }

    @Test
    void should_ThrowException_When_ReturnDoesNotBelongToInvoice() {
        Invoice invoice = Invoice.reconstitute(
                InvoiceId.generate(), INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, null, null,
                InvoiceStatus.PAID, PaymentType.CASH, null,
                BigDecimal.ZERO, null,
                null, null, validLines, LocalDateTime.now(), LocalDateTime.now());

        Return return_ = mock(Return.class);
        when(return_.getInvoiceId()).thenReturn(InvoiceId.generate());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> invoice.revertReturn(return_));

        assertEquals("La devolución no pertenece a esta factura", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_ReturnIsNull() {
        Invoice invoice = Invoice.reconstitute(
                InvoiceId.generate(), INVOICE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, null, null,
                InvoiceStatus.PAID, PaymentType.CASH, null,
                BigDecimal.ZERO, null,
                null, null, validLines, LocalDateTime.now(), LocalDateTime.now());

        assertThrows(NullPointerException.class, () -> invoice.revertReturn(null));
    }
}
