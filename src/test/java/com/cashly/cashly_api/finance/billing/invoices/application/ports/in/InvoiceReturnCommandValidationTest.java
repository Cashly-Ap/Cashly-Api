package com.cashly.cashly_api.finance.billing.invoices.application.ports.in;

import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceReturnAdjustment;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class InvoiceReturnCommandValidationTest {
    private final UUID invoiceId = UUID.randomUUID();
    private final UUID lineId = UUID.randomUUID();

    @Test
    void shouldRejectEmptyLinesAtBothBoundaries() {
        assertThrows(IllegalArgumentException.class, () -> new InvoiceReturnAdjustment(invoiceId, BigDecimal.TEN, List.of(), true));
        assertThrows(IllegalArgumentException.class, () -> new InvoiceReturnCommand(invoiceId, BigDecimal.TEN, List.of(), true));
    }

    @Test
    void shouldRejectNegativeTotalsAtBothBoundaries() {
        assertThrows(IllegalArgumentException.class, () -> new InvoiceReturnAdjustment(invoiceId, BigDecimal.ONE.negate(), domainLines(), false));
        assertThrows(IllegalArgumentException.class, () -> new InvoiceReturnCommand(invoiceId, BigDecimal.ONE.negate(), apiLines(), false));
    }

    @Test
    void shouldRejectNullInvoiceIdsAndTotalsAtBothBoundaries() {
        assertThrows(NullPointerException.class, () -> new InvoiceReturnAdjustment(null, BigDecimal.TEN, domainLines(), false));
        assertThrows(NullPointerException.class, () -> new InvoiceReturnCommand(null, BigDecimal.TEN, apiLines(), false));
        assertThrows(NullPointerException.class, () -> new InvoiceReturnAdjustment(invoiceId, null, domainLines(), false));
        assertThrows(NullPointerException.class, () -> new InvoiceReturnCommand(invoiceId, null, apiLines(), false));
    }

    @Test
    void shouldRejectInvalidLineValuesAtBothBoundaries() {
        assertThrows(NullPointerException.class, () -> new InvoiceReturnAdjustment.Line(null, BigDecimal.ONE));
        assertThrows(NullPointerException.class, () -> new InvoiceReturnCommand.Line(null, BigDecimal.ONE));
        assertThrows(NullPointerException.class, () -> new InvoiceReturnAdjustment.Line(lineId, null));
        assertThrows(NullPointerException.class, () -> new InvoiceReturnCommand.Line(lineId, null));
        for (BigDecimal quantity : List.of(BigDecimal.ZERO, BigDecimal.ONE.negate())) {
            assertThrows(IllegalArgumentException.class, () -> new InvoiceReturnAdjustment.Line(lineId, quantity));
            assertThrows(IllegalArgumentException.class, () -> new InvoiceReturnCommand.Line(lineId, quantity));
        }
    }

    @Test
    void shouldPreserveValidZeroTotalAndImmutableLines() {
        var command = new InvoiceReturnCommand(invoiceId, BigDecimal.ZERO, apiLines(), false);
        assertEquals(BigDecimal.ZERO, command.total());
        assertThrows(UnsupportedOperationException.class, () -> command.lines().clear());
        var adjustment = new InvoiceReturnAdjustment(invoiceId, BigDecimal.ZERO, domainLines(), false);
        assertThrows(UnsupportedOperationException.class, () -> adjustment.lines().clear());
    }

    private List<InvoiceReturnAdjustment.Line> domainLines() {
        return List.of(new InvoiceReturnAdjustment.Line(lineId, BigDecimal.ONE));
    }
    private List<InvoiceReturnCommand.Line> apiLines() {
        return List.of(new InvoiceReturnCommand.Line(lineId, BigDecimal.ONE));
    }
}