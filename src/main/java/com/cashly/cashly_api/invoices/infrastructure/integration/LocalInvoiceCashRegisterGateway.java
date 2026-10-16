package com.cashly.cashly_api.invoices.infrastructure.integration;
import com.cashly.cashly_api.invoices.application.ports.out.CashRegisterGateway;
import com.cashly.cashly_api.cash_register.sessions.application.ports.in.CashRegisterOperations;
import org.springframework.stereotype.Component;
@Component
public class LocalInvoiceCashRegisterGateway implements CashRegisterGateway {
    private final CashRegisterOperations operations;
    public LocalInvoiceCashRegisterGateway(CashRegisterOperations operations) { this.operations = operations; }
    public void requireOpenRegister() { operations.requireOpenRegister(); }
    public void recordInvoice(InvoiceEntry entry) {
        operations.record(new CashRegisterOperations.Entry(CashRegisterOperations.Direction.IN, CashRegisterOperations.EntryType.INVOICE,
                entry.amount(), null, "Factura confirmada " + entry.invoiceNumber(), "INVOICE", entry.invoiceId(), entry.actorId(), entry.actorName()));
    }
}
