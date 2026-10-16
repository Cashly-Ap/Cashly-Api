package com.cashly.cashly_api.orders.infrastructure.integration;

import com.cashly.cashly_api.invoices.application.ports.in.OrderInvoiceCreationApi;
import com.cashly.cashly_api.orders.application.ports.out.InvoiceCreationGateway;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class LocalInvoiceCreationGateway implements InvoiceCreationGateway {
    private final OrderInvoiceCreationApi api;

    public LocalInvoiceCreationGateway(OrderInvoiceCreationApi api) {
        this.api = api;
    }

    @Override
    public CreatedInvoice create(Command command, UUID actorId) {
        var invoice = api.create(new OrderInvoiceCreationApi.Command(command.orderId(), command.paymentType(),
                command.useBalance(), command.balanceAmount(), command.paymentMethodId(),
                command.confirmationPassword(), command.issueDate()), actorId);
        return new CreatedInvoice(invoice.id(), invoice.invoiceNumber());
    }
}
