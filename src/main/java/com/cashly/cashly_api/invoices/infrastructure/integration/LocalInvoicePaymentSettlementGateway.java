package com.cashly.cashly_api.invoices.infrastructure.integration;
import com.cashly.cashly_api.invoices.application.contracts.InvoicePaymentSettlementGateway;
import com.cashly.cashly_api.payments.application.api.CashInvoiceSettlement;
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
