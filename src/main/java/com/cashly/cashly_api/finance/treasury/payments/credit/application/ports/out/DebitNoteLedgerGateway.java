package com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public interface DebitNoteLedgerGateway {
    List<BigDecimal> amountsByInvoiceId(UUID invoiceId);
}
