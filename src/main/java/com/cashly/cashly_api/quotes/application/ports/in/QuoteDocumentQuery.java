package com.cashly.cashly_api.quotes.application.ports.in;

import java.util.List;
import java.util.UUID;

public interface QuoteDocumentQuery {
    QuoteDocumentSnapshot find(UUID id);
    List<QuoteDocumentRow> findAll(QuoteDocumentFilter filter);
}
