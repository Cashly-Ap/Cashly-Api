package com.cashly.cashly_api.payments.infrastructure.adapters.out.integration;
import com.cashly.cashly_api.payments.application.ports.out.DebitNoteLedgerGateway;
import com.cashly.cashly_api.debit_notes.application.ports.in.DebitNoteLedgerQuery;
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
