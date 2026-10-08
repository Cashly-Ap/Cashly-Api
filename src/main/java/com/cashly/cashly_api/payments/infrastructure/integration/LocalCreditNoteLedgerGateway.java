package com.cashly.cashly_api.payments.infrastructure.integration;
import com.cashly.cashly_api.payments.application.contracts.CreditNoteLedgerGateway;
import com.cashly.cashly_api.credit_notes.application.api.CreditNoteLedgerQuery;
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
