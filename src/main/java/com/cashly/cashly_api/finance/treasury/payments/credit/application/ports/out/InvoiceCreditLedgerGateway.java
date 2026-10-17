package com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out;

import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.model.InvoiceCreditEntry;
import java.util.List;
import java.util.UUID;
public interface InvoiceCreditLedgerGateway {
    List<InvoiceCreditEntry> findByCustomerId(UUID customerId);
    List<InvoiceCreditEntry> findAll();
}
