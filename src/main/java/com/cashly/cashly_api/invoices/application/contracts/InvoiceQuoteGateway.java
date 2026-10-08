package com.cashly.cashly_api.invoices.application.contracts;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceQuoteGateway {
    Optional<Quote> findQuote(UUID quoteId);

    record Quote(UUID id, UUID customerId, String status) {}
}
