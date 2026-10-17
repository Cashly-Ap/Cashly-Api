package com.cashly.cashly_api.finance.treasury.payments.credit.infrastructure.integration;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.CreditNoteLedgerGateway;
import com.cashly.cashly_api.finance.billing.credit_notes.application.ports.in.CreditNoteLedgerQuery;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
@Component
public class LocalCreditNoteLedgerGateway implements CreditNoteLedgerGateway {
    private final CreditNoteLedgerQuery query;
    public LocalCreditNoteLedgerGateway(CreditNoteLedgerQuery query) { this.query = query; }
    public List<BigDecimal> amountsByInvoiceId(UUID id) { return query.amountsByInvoiceId(id); }
}
