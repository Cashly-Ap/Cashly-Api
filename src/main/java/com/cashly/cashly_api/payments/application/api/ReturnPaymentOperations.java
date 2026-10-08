package com.cashly.cashly_api.payments.application.api;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public interface ReturnPaymentOperations {
    BigDecimal allocatedAmount(UUID invoiceId);
    List<AllocationSnapshot> capture(UUID invoiceId);
    void cancelInvoicePayments(UUID invoiceId);
    void restore(List<AllocationSnapshot> snapshots);
    record AllocationSnapshot(UUID paymentId, UUID invoiceId, BigDecimal amount) {}
}
