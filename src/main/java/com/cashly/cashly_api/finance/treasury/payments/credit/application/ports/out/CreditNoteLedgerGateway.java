package com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
public interface CreditNoteLedgerGateway {
    List<BigDecimal> amountsByInvoiceId(UUID invoiceId);
    Map<UUID, BigDecimal> totalsByInvoiceIds(List<UUID> invoiceIds);
}
