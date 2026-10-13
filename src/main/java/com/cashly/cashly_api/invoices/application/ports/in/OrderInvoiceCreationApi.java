package com.cashly.cashly_api.invoices.application.ports.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface OrderInvoiceCreationApi {
    CreatedInvoice create(Command command, UUID actorId);
    record Command(UUID orderId, String paymentType, Boolean useBalance, BigDecimal balanceAmount,
                   UUID paymentMethodId, String confirmationPassword, LocalDate issueDate) {}
    record CreatedInvoice(UUID id, String invoiceNumber) {}
}
