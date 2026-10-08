package com.cashly.cashly_api.payments.application.contracts;
import java.util.List;
import java.util.UUID;
public interface InvoiceCreditLedgerGateway {
    List<InvoiceCreditEntry> findByCustomerId(UUID customerId);
    List<InvoiceCreditEntry> findAll();
}
