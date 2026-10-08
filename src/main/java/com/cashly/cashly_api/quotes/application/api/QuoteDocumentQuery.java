package com.cashly.cashly_api.quotes.application.api;

import java.util.List;
import java.util.UUID;

public interface QuoteDocumentQuery {
    QuoteDocumentSnapshot find(UUID id);
    List<QuoteDocumentRow> findAll(QuoteDocumentFilter filter);
}
