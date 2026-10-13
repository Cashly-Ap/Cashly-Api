package com.cashly.cashly_api.invoices.infrastructure.adapters.out.integration;
import com.cashly.cashly_api.invoices.application.ports.out.InvoicePaymentSettlementGateway;
import com.cashly.cashly_api.payments.application.ports.in.CashInvoiceSettlement;
import org.springframework.stereotype.Component;
@Component
public class LocalInvoicePaymentSettlementGateway implements InvoicePaymentSettlementGateway {
    private final CashInvoiceSettlement settlement;
    public LocalInvoicePaymentSettlementGateway(CashInvoiceSettlement settlement) { this.settlement = settlement; }
    public void settleCashInvoice(Settlement command) {
        settlement.settle(new CashInvoiceSettlement.Command(command.customerId(), command.invoiceId(), command.invoiceNumber(),
                command.amount(), command.paymentMethodId(), command.actorId(), command.actorName()));
    }
}
