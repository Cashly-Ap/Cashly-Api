package com.cashly.cashly_api.finance.billing.invoices.application.create.from_order.services;

import com.cashly.cashly_api.finance.billing.invoices.application.ports.in.OrderInvoiceCreationApi;
import com.cashly.cashly_api.finance.billing.invoices.application.create.request.CreateInvoiceRequest;
import com.cashly.cashly_api.finance.billing.invoices.application.create.use_case.CreateInvoiceUseCase;
import java.util.UUID;

public class OrderInvoiceCreationService implements OrderInvoiceCreationApi {
    private final CreateInvoiceUseCase createInvoice;

    public OrderInvoiceCreationService(CreateInvoiceUseCase createInvoice) {
        this.createInvoice = createInvoice;
    }

    @Override
    public CreatedInvoice create(Command command, UUID actorId) {
        var request = new CreateInvoiceRequest(command.orderId(), null, null,
                command.paymentType(), null, command.useBalance(),
                command.balanceAmount(), command.paymentMethodId(),
                command.confirmationPassword(), null, command.issueDate());
        var invoice = createInvoice.execute(request, actorId);
        return new CreatedInvoice(invoice.id(), invoice.invoiceNumber());
    }
}
