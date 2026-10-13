package com.cashly.cashly_api.invoices.infrastructure.adapters.out.integration;
import com.cashly.cashly_api.invoices.application.ports.out.CustomerBalanceGateway;
import com.cashly.cashly_api.customers.application.ports.in.InvoiceBalanceConsumption;
import org.springframework.stereotype.Component;
@Component
public class LocalCustomerBalanceGateway implements CustomerBalanceGateway {
    private final InvoiceBalanceConsumption consumption;
    public LocalCustomerBalanceGateway(InvoiceBalanceConsumption consumption) { this.consumption = consumption; }
    public void consumeInvoiceBalance(Consumption command) {
        consumption.consume(new InvoiceBalanceConsumption.Command(command.customerId(), command.amount(), command.invoiceId(), command.invoiceNumber(), command.actorId()));
    }
}
