package com.cashly.cashly_api.invoices.domain.valueobjects;

import com.cashly.cashly_api.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.invoices.domain.entity.InvoiceLine;

import com.cashly.cashly_api.invoices.domain.exceptions.InvoiceNotReturnableException;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceReturnAdjustment;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceReturnStatus;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.invoices.domain.valueobjects.PaymentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceReturnAdjustmentTest {
    @Test
    void shouldAccumulatePartialReturnWithoutClosingInvoice() {
        Invoice invoice = invoice();
        invoice.applyReturn(adjustment(invoice, BigDecimal.ONE, false));
        assertEquals(BigDecimal.TEN, invoice.getReturnedTotal());
        assertEquals(BigDecimal.ONE, invoice.getLines().getFirst().getReturnedQuantity());
        assertEquals(InvoiceReturnStatus.PARTIAL, invoice.getReturnStatus());
        assertEquals(InvoiceStatus.PENDING_PAYMENT, invoice.getStatus());
    }

    @Test
    void shouldCloseInvoiceWhenEveryQuantityIsReturned() {
        Invoice invoice = invoice();
        invoice.applyReturn(adjustment(invoice, new BigDecimal("2"), false));
        assertEquals(InvoiceReturnStatus.FULL, invoice.getReturnStatus());
        assertEquals(InvoiceStatus.FULLY_RETURNED, invoice.getStatus());
    }

    @Test
    void shouldCloseAutomaticReturnEvenWhenQuantityIsPartial() {
        Invoice invoice = invoice();
        invoice.applyReturn(adjustment(invoice, BigDecimal.ONE, true));
        assertEquals(InvoiceReturnStatus.PARTIAL, invoice.getReturnStatus());
        assertEquals(InvoiceStatus.FULLY_RETURNED, invoice.getStatus());
    }

    @Test
    void shouldRejectReturnForCancelledInvoice() {
        Invoice invoice = invoice();
        invoice.cancel("Cancelled");
        assertThrows(InvoiceNotReturnableException.class,
                () -> invoice.applyReturn(adjustment(invoice, BigDecimal.ONE, false)));
    }

    @Test
    void shouldRejectReturnForAnotherInvoice() {
        Invoice invoice = invoice();
        InvoiceReturnAdjustment other = new InvoiceReturnAdjustment(UUID.randomUUID(), BigDecimal.TEN, List.of(new InvoiceReturnAdjustment.Line(UUID.randomUUID(), BigDecimal.ONE)), false);
        assertThrows(IllegalArgumentException.class, () -> invoice.applyReturn(other));
        assertEquals(BigDecimal.ZERO, invoice.getReturnedTotal());
    }

    @Test
    void shouldReverseAmountAndQuantityUsingOwnedAdjustment() {
        Invoice invoice = invoice();
        InvoiceReturnAdjustment adjustment = adjustment(invoice, BigDecimal.ONE, false);
        invoice.applyReturn(adjustment);
        invoice.revertReturn(adjustment);
        assertEquals(BigDecimal.ZERO, invoice.getReturnedTotal());
        assertEquals(BigDecimal.ZERO, invoice.getLines().getFirst().getReturnedQuantity());
        assertNull(invoice.getReturnStatus());
    }

    @Test
    void shouldRejectUnknownLineBeforeApplyingAnyMutation() {
        Invoice invoice = invoice();
        InvoiceReturnAdjustment invalid = new InvoiceReturnAdjustment(invoice.getId().value(), BigDecimal.TEN,
                List.of(new InvoiceReturnAdjustment.Line(invoice.getLines().getFirst().getId().value(), BigDecimal.ONE),
                        new InvoiceReturnAdjustment.Line(UUID.randomUUID(), BigDecimal.ONE)), true);
        assertThrows(IllegalArgumentException.class, () -> invoice.applyReturn(invalid));
        assertEquals(BigDecimal.ZERO, invoice.getReturnedTotal());
        assertEquals(BigDecimal.ZERO, invoice.getLines().getFirst().getReturnedQuantity());
        assertEquals(InvoiceStatus.PENDING_PAYMENT, invoice.getStatus());
    }

    @Test
    void shouldRejectUnknownLineBeforeReversingAnyMutation() {
        Invoice invoice = invoice();
        invoice.applyReturn(adjustment(invoice, BigDecimal.ONE, false));
        InvoiceReturnAdjustment invalid = new InvoiceReturnAdjustment(invoice.getId().value(), BigDecimal.TEN,
                List.of(new InvoiceReturnAdjustment.Line(invoice.getLines().getFirst().getId().value(), BigDecimal.ONE),
                        new InvoiceReturnAdjustment.Line(UUID.randomUUID(), BigDecimal.ONE)), false);
        assertThrows(IllegalArgumentException.class, () -> invoice.revertReturn(invalid));
        assertEquals(BigDecimal.TEN, invoice.getReturnedTotal());
        assertEquals(BigDecimal.ONE, invoice.getLines().getFirst().getReturnedQuantity());
        assertEquals(InvoiceReturnStatus.PARTIAL, invoice.getReturnStatus());
    }
    private Invoice invoice() {
        InvoiceLine line = InvoiceLine.create(UUID.randomUUID(), "Product", "SKU", new BigDecimal("2"), BigDecimal.TEN, List.of());
        Invoice invoice = Invoice.create(InvoiceNumber.of(2026, 1), UUID.randomUUID(), "Customer", "RIF",
                UUID.randomUUID(), "User", null, null, PaymentType.CREDIT, null, List.of(line));
        invoice.confirm();
        return invoice;
    }

    private InvoiceReturnAdjustment adjustment(Invoice invoice, BigDecimal quantity, boolean close) {
        return new InvoiceReturnAdjustment(invoice.getId().value(), quantity.multiply(BigDecimal.TEN),
                List.of(new InvoiceReturnAdjustment.Line(invoice.getLines().getFirst().getId().value(), quantity)), close);
    }
}
