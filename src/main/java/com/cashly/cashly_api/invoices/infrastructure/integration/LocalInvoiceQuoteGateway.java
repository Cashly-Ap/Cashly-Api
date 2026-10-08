package com.cashly.cashly_api.invoices.infrastructure.integration;

import com.cashly.cashly_api.invoices.application.contracts.InvoiceQuoteGateway;
import com.cashly.cashly_api.quotes.domain.repositories.QuoteRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class LocalInvoiceQuoteGateway implements InvoiceQuoteGateway {
    private final QuoteRepository quotes;

    public LocalInvoiceQuoteGateway(QuoteRepository quotes) {
        this.quotes = quotes;
    }

    @Override
    public Optional<Quote> findQuote(UUID quoteId) {
        return quotes.findById(quoteId).map(quote -> new Quote(
                quote.getId(), quote.getCustomerId(), quote.getStatus().name()));
    }
}
