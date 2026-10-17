package com.cashly.cashly_api.finance.billing.invoices.application.queries.services;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.in.ReturnInvoiceQuery;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
public class RepositoryReturnInvoiceQuery implements ReturnInvoiceQuery {
    private final InvoiceRepository invoices;
    public RepositoryReturnInvoiceQuery(InvoiceRepository invoices) { this.invoices = invoices; }
    public Optional<Projection> findById(UUID invoiceId) {
        return invoices.findById(InvoiceId.of(invoiceId)).map(invoice -> new Projection(invoice.getInvoiceNumber().value(),
                invoice.getCustomerName(), invoice.getCustomerRif(), invoice.getStatus() == InvoiceStatus.CANCELLED,
                invoice.calculateTotal(BigDecimal.ZERO, BigDecimal.ZERO), invoice.getSubtotal() != null));
    }
}
