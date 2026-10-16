package com.cashly.cashly_api.payments.credit.application.ports.out;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public interface CreditNoteLedgerGateway {
    List<BigDecimal> amountsByInvoiceId(UUID invoiceId);
}
