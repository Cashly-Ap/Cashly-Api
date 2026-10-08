package com.cashly.cashly_api.payments.infrastructure.integration;
import com.cashly.cashly_api.payments.application.contracts.DebitNoteLedgerGateway;
import com.cashly.cashly_api.debit_notes.application.api.DebitNoteLedgerQuery;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
@Component
public class LocalDebitNoteLedgerGateway implements DebitNoteLedgerGateway {
    private final DebitNoteLedgerQuery query;
    public LocalDebitNoteLedgerGateway(DebitNoteLedgerQuery query) { this.query = query; }
    public List<BigDecimal> amountsByInvoiceId(UUID id) { return query.amountsByInvoiceId(id); }
}
