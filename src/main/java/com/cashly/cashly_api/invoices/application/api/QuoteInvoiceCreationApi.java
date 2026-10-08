package com.cashly.cashly_api.invoices.application.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface QuoteInvoiceCreationApi {
    CreatedInvoice create(Command command, UUID actorId);
    record Command(UUID customerId, String customerName, String customerRif, String paymentType,
                   Boolean useBalance, BigDecimal balanceAmount, UUID paymentMethodId, String confirmationPassword,
                   LocalDate issueDate, List<Line> lines) {
        public Command { lines = List.copyOf(lines); }
    }
    record Line(UUID productId, String productName, String productCode, BigDecimal quantity,
                BigDecimal unitPrice, List<Allocation> allocations) {
        public Line { allocations = List.copyOf(allocations); }
    }
    record Allocation(UUID warehouseId, UUID productId, BigDecimal quantity) {}
    record CreatedInvoice(UUID id, String number) {}
}
