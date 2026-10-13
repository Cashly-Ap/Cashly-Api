package com.cashly.cashly_api.quotes.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.invoices.application.ports.in.QuoteInvoiceCreationApi;
import com.cashly.cashly_api.quotes.application.ports.out.InvoiceCreationGateway;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component("quoteInvoiceCreationGateway")
public class LocalInvoiceCreationGateway implements InvoiceCreationGateway {
    private final QuoteInvoiceCreationApi api;

    public LocalInvoiceCreationGateway(QuoteInvoiceCreationApi api) {
        this.api = api;
    }

    @Override
    public CreatedInvoice create(Command command, UUID actorId) {
        var lines = command.lines().stream().map(line -> new QuoteInvoiceCreationApi.Line(line.productId(),
                line.productName(), line.productCode(), line.quantity(), line.unitPrice(), line.allocations().stream()
                        .map(item -> new QuoteInvoiceCreationApi.Allocation(item.warehouseId(), item.productId(), item.quantity()))
                        .toList())).toList();
        var invoice = api.create(new QuoteInvoiceCreationApi.Command(command.customerId(), command.customerName(),
                command.customerRif(), command.paymentType(), command.useBalance(), command.balanceAmount(),
                command.paymentMethodId(), command.confirmationPassword(), command.issueDate(), lines), actorId);
        return new CreatedInvoice(invoice.id(), invoice.number());
    }
}
