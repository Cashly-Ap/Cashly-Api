package com.cashly.cashly_api.sales.quotes.application.convert_to_invoice.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.sales.quotes.application.ports.out.InvoiceCreationGateway;
import com.cashly.cashly_api.sales.quotes.application.convert_to_invoice.request.ConvertQuoteToInvoiceRequest;
import com.cashly.cashly_api.sales.quotes.application.convert_to_invoice.response.QuoteConvertedToInvoiceResponse;
import com.cashly.cashly_api.sales.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.sales.quotes.domain.repository.QuoteRepository;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteStatus;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class ConvertQuoteToInvoiceUseCase {
    private final QuoteRepository quotes;
    private final InvoiceCreationGateway invoices;

    private final ActivityNotificationRecorder activityNotifications;

    public ConvertQuoteToInvoiceUseCase(QuoteRepository quotes,
            InvoiceCreationGateway invoices,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.quotes = quotes;
        this.invoices = invoices;
    }

    @Transactional
    @Audited(module = "sales", entityType = "QUOTE", action = "CONVERT_TO_INVOICE")
    public QuoteConvertedToInvoiceResponse execute(UUID quoteId,
            ConvertQuoteToInvoiceRequest request, UUID authenticatedUserId) {
        var quote = quotes.findById(quoteId)
                .orElseThrow(() -> new QuoteNotFoundException(quoteId));
        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only DRAFT quotes can be converted to invoices");
        }
        var lines = quote.getLines().stream()
                .map(line -> new InvoiceCreationGateway.Line(
                        line.getProductId(), line.getProductName(),
                        line.getProductCode(), line.getQuantity(),
                        line.getUnitPrice(),
                        line.getAllocations().stream().map(
                                allocation -> new InvoiceCreationGateway.Allocation(
                                        allocation.getWarehouseId(),
                                        allocation.getProductId(),
                                        allocation.getAllocatedQuantity()))
                                .toList()))
                .toList();
        var invoice = invoices.create(new InvoiceCreationGateway.Command(
                quote.getCustomerId(), quote.getCustomerName(),
                quote.getCustomerRif(), request.paymentType(),
                request.useBalance(), request.balanceAmount(),
                request.paymentMethodId(), request.confirmationPassword(),
                request.issueDate(), lines), authenticatedUserId);
        quote.markConvertedToInvoice(invoice.id(), invoice.number());
        quotes.save(quote);
        activityNotifications.record(ActivityEvent.of("SALES_QUOTES",
                "CONVERT_TO_INVOICE", "QUOTE", quote.getId(),
                quote.getQuoteNumber().value(), authenticatedUserId));
        return new QuoteConvertedToInvoiceResponse(quote.getId(),
                quote.getQuoteNumber().value(), invoice.id(), invoice.number());
    }
}
