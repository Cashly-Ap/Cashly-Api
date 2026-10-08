package com.cashly.cashly_api.returns.application.contracts;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface ReturnInvoiceQueryGateway {
    Optional<InvoiceSnapshot> findById(UUID invoiceId);
    InvoiceSnapshot requireById(UUID invoiceId);
    List<BigDecimal> creditNoteAmounts(UUID invoiceId);
    List<BigDecimal> debitNoteAmounts(UUID invoiceId);
    record InvoiceSnapshot(String number, boolean cancelled, BigDecimal baseTotal, boolean hasSubtotal) {
        public BigDecimal calculateTotal(BigDecimal credits, BigDecimal debits) {
            return hasSubtotal ? baseTotal.subtract(credits).add(debits) : BigDecimal.ZERO;
        }
    }
}
