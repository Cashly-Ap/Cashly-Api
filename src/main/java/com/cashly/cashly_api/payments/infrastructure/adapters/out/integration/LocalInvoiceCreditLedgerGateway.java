package com.cashly.cashly_api.payments.infrastructure.adapters.out.integration;
import com.cashly.cashly_api.payments.application.ports.out.model.InvoiceCreditEntry;
import com.cashly.cashly_api.payments.application.ports.out.InvoiceCreditLedgerGateway;
import com.cashly.cashly_api.invoices.application.ports.in.InvoiceCreditLedgerQuery;
import com.cashly.cashly_api.invoices.application.ports.in.InvoiceCreditLedgerEntry;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;
@Component
public class LocalInvoiceCreditLedgerGateway implements InvoiceCreditLedgerGateway {
    private final InvoiceCreditLedgerQuery query;
    public LocalInvoiceCreditLedgerGateway(InvoiceCreditLedgerQuery query) { this.query = query; }
    public List<InvoiceCreditEntry> findByCustomerId(UUID id) { return map(query.findByCustomerId(id)); }
    public List<InvoiceCreditEntry> findAll() { return map(query.findAll()); }
    private List<InvoiceCreditEntry> map(List<InvoiceCreditLedgerEntry> entries) {
        return entries.stream().map(entry -> new InvoiceCreditEntry(entry.id(), entry.number(), entry.pendingPayment(),
                entry.subtotal(), entry.returnedTotal(), entry.dueDate())).toList();
    }
}
