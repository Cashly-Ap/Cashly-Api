package com.cashly.cashly_api.finance.billing.returns.application.ports.out;
import java.util.Optional;
import java.util.UUID;
public interface ReturnProjectionQueryGateway {
    Optional<InvoiceLabels> findInvoice(UUID invoiceId);
    Optional<String> findWarehouseName(UUID warehouseId);
    record InvoiceLabels(String number, String customerName,
            String customerRif) {
    }
}
