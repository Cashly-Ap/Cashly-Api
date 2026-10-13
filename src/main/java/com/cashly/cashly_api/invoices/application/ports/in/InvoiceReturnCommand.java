package com.cashly.cashly_api.invoices.application.ports.in;

import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceReturnAdjustment;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record InvoiceReturnCommand(UUID invoiceId, BigDecimal total, List<Line> lines, boolean closeInvoice) {
    public InvoiceReturnCommand {
        lines = List.copyOf(lines);
        new InvoiceReturnAdjustment(invoiceId, total, lines.stream()
                .map(line -> new InvoiceReturnAdjustment.Line(line.invoiceLineId(), line.quantity())).toList(), closeInvoice);
    }

    public record Line(UUID invoiceLineId, BigDecimal quantity) {
        public Line {
            new InvoiceReturnAdjustment.Line(invoiceLineId, quantity);
        }
    }
}
