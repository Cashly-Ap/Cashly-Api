package com.cashly.cashly_api.payments.application.contracts;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public interface CreditNoteLedgerGateway {
    List<BigDecimal> amountsByInvoiceId(UUID invoiceId);
}
