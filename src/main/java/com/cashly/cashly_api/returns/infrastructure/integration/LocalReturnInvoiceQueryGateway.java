package com.cashly.cashly_api.returns.infrastructure.integration;
import com.cashly.cashly_api.returns.application.contracts.ReturnInvoiceQueryGateway;
import com.cashly.cashly_api.invoices.application.api.ReturnInvoiceQuery;
import com.cashly.cashly_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.cashly.cashly_api.credit_notes.application.api.CreditNoteLedgerQuery;
import com.cashly.cashly_api.debit_notes.application.api.DebitNoteLedgerQuery;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Component
public class LocalReturnInvoiceQueryGateway implements ReturnInvoiceQueryGateway {
    private final ReturnInvoiceQuery invoices;
    private final CreditNoteLedgerQuery credits;
    private final DebitNoteLedgerQuery debits;
    public LocalReturnInvoiceQueryGateway(ReturnInvoiceQuery invoices, CreditNoteLedgerQuery credits, DebitNoteLedgerQuery debits) {
        this.invoices = invoices; this.credits = credits; this.debits = debits;
    }
    public Optional<InvoiceSnapshot> findById(UUID invoiceId) {
        return invoices.findById(invoiceId).map(invoice -> new InvoiceSnapshot(invoice.number(), invoice.cancelled(), invoice.baseTotal(), invoice.hasSubtotal()));
    }
    public InvoiceSnapshot requireById(UUID invoiceId) { return findById(invoiceId).orElseThrow(() -> new InvoiceNotFoundException(invoiceId)); }
    public List<BigDecimal> creditNoteAmounts(UUID invoiceId) { return credits.amountsByInvoiceId(invoiceId); }
    public List<BigDecimal> debitNoteAmounts(UUID invoiceId) { return debits.amountsByInvoiceId(invoiceId); }
}
