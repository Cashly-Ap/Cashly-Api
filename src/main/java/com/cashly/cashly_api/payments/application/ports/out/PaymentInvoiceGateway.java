package com.cashly.cashly_api.payments.application.ports.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentInvoiceGateway {
    Optional<InvoiceSnapshot> findById(UUID invoiceId);
    List<InvoiceSnapshot> findUnpaidByCustomerId(UUID customerId);
    void recalculateStatus(UUID invoiceId);

    record InvoiceSnapshot(UUID id, UUID customerId, String number, BigDecimal total) {}
}
