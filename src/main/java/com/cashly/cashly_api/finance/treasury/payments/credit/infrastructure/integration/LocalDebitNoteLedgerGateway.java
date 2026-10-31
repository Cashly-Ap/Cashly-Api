package com.cashly.cashly_api.finance.treasury.payments.credit.infrastructure.integration;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.DebitNoteLedgerGateway;
import com.cashly.cashly_api.finance.billing.debit_notes.application.ports.in.DebitNoteLedgerQuery;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
@Component
public class LocalDebitNoteLedgerGateway implements DebitNoteLedgerGateway {
    private final DebitNoteLedgerQuery query;
    public LocalDebitNoteLedgerGateway(DebitNoteLedgerQuery query) {
        this.query = query;
    }
    public List<BigDecimal> amountsByInvoiceId(UUID id) {
        return query.amountsByInvoiceId(id);
    }
}
