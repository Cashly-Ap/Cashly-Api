package com.cashly.cashly_api.finance.billing.invoices.application.ports.out;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceQuoteGateway {
    Optional<Quote> findQuote(UUID quoteId);

    record Quote(UUID id, UUID customerId, String status) {}
}
